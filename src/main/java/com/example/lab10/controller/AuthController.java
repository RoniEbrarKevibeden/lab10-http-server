package com.example.lab10.controller;

import com.example.lab10.dto_.LoginRequest;
import com.example.lab10.dto_.UserCreateRequest;
import com.example.lab10.entity.AppUser;
import com.example.lab10.repo.UserRepository;
import com.example.lab10.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserCreateRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("ok", false, "error", "email_taken", 
                                 "message", "Email is already registered"));
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("ok", false, "error", "username_taken",
                                 "message", "Username is already taken"));
        }

        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("ok", true, "username", user.getUsername(), 
                             "message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "invalid_credentials",
                                 "message", "Invalid username or password"));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "account_disabled",
                                 "message", "Account is disabled"));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", "account_locked",
                                 "message", "Account is locked"));
        }

        AppUser user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                List.of(user.getRole())
        );

        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60); // 1 hour
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(Map.of("ok", true, "message", "Logged out successfully"));
    }
}
