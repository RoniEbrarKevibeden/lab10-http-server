package com.example.lab10.controller;

import com.example.lab10.dto_.LoginRequest;
import com.example.lab10.dto_.RefreshTokenRequest;
import com.example.lab10.dto_.TokenResponse;
import com.example.lab10.dto_.UserCreateRequest;
import com.example.lab10.entity.AppUser;
import com.example.lab10.entity.RefreshToken;
import com.example.lab10.repo.UserRepository;
import com.example.lab10.security.JwtUtil;
import com.example.lab10.security.RateLimiter;
import com.example.lab10.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final RateLimiter rateLimiter;

    @Value("${app.jwt.exp-minutes:15}")
    private long accessTokenExpMinutes;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          TokenService tokenService,
                          RateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserCreateRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            logger.warn("Registration failed: email already taken");
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", "email_taken"));
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            logger.warn("Registration failed: username already taken");
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", "username_taken"));
        }

        AppUser u = new AppUser();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole("ROLE_USER");

        userRepository.save(u);
        logger.info("New user registered: {}", maskUsername(req.getUsername()));

        return ResponseEntity.ok(Map.of("ok", true, "username", u.getUsername()));
    }

    /**
     * Login and receive access token + refresh token.
     * Implements secure cookie attributes for refresh token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String clientIp = getClientIp(request);
        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            // Reset rate limit on successful login
            rateLimiter.resetLimit(clientIp);

            AppUser user = userRepository.findByUsername(req.getUsername()).orElseThrow();

            // Generate access token (short-lived)
            String accessToken = jwtUtil.generateToken(
                    user.getUsername(),
                    List.of(user.getRole())
            );

            // Generate refresh token (long-lived) with rotation
            RefreshToken refreshToken = tokenService.createRefreshToken(user.getUsername());

            // Set refresh token in HttpOnly cookie (more secure than sending in response)
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
            refreshCookie.setHttpOnly(true);    // Not accessible via JavaScript
            refreshCookie.setSecure(true);      // Only sent over HTTPS
            refreshCookie.setPath("/auth");     // Only sent to auth endpoints
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            // SameSite attribute (set via header for better compatibility)
            response.addHeader("Set-Cookie", 
                    String.format("refreshToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/auth; Max-Age=%d",
                            refreshToken.getToken(), 7 * 24 * 60 * 60));

            logger.info("User logged in successfully: {}", maskUsername(user.getUsername()));

            // Return tokens
            TokenResponse tokenResponse = new TokenResponse(
                    accessToken,
                    refreshToken.getToken(),
                    accessTokenExpMinutes * 60
            );

            return ResponseEntity.ok(tokenResponse);

        } catch (AuthenticationException e) {
            // Record failed login for rate limiting
            rateLimiter.recordFailedLogin(clientIp);
            
            // Log failed attempt (no password!)
            logger.warn("Failed login attempt for user: {} from IP: {}", 
                    maskUsername(req.getUsername()), clientIp);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_credentials", "message", "Invalid username or password"));
        }
    }

    /**
     * Refresh access token using refresh token.
     * Implements token rotation - old refresh token is invalidated.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest req,
                                          HttpServletResponse response) {
        
        String refreshTokenStr = req.getRefreshToken();
        
        // Validate and rotate the refresh token
        Optional<RefreshToken> newTokenOpt = tokenService.rotateRefreshToken(refreshTokenStr);
        
        if (newTokenOpt.isEmpty()) {
            logger.warn("Invalid or expired refresh token used");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_refresh_token", 
                                 "message", "Refresh token is invalid, expired, or already used"));
        }

        RefreshToken newRefreshToken = newTokenOpt.get();
        AppUser user = userRepository.findByUsername(newRefreshToken.getUsername()).orElseThrow();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getUsername(),
                List.of(user.getRole())
        );

        // Update refresh token cookie
        response.addHeader("Set-Cookie", 
                String.format("refreshToken=%s; HttpOnly; Secure; SameSite=Strict; Path=/auth; Max-Age=%d",
                        newRefreshToken.getToken(), 7 * 24 * 60 * 60));

        logger.info("Token refreshed for user: {}", maskUsername(user.getUsername()));

        TokenResponse tokenResponse = new TokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                accessTokenExpMinutes * 60
        );

        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * Logout - invalidates both access token and all refresh tokens.
     * Implements session invalidation.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        
        String authHeader = request.getHeader("Authorization");
        String username = null;
        
        // Blacklist the current access token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenService.blacklistAccessToken(accessToken);
            
            // Get username from security context
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                username = auth.getName();
            }
        }
        
        // Revoke all refresh tokens for this user
        if (username != null) {
            tokenService.revokeAllUserTokens(username);
            logger.info("User logged out: {}", maskUsername(username));
        }
        
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Clear refresh token cookie
        response.addHeader("Set-Cookie", 
                "refreshToken=; HttpOnly; Secure; SameSite=Strict; Path=/auth; Max-Age=0");
        
        return ResponseEntity.ok(Map.of("ok", true, "message", "Logged out successfully"));
    }

    /**
     * Helper method to mask username in logs for privacy.
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.substring(0, 2) + "***";
    }

    /**
     * Helper method to get real client IP considering proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
