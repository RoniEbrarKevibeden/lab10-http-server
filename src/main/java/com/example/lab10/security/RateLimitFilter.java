package com.example.lab10.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that applies rate limiting to incoming requests.
 * Runs before authentication to prevent brute-force attacks.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimiter rateLimiter;

    public RateLimitFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        // Apply stricter rate limiting to auth endpoints
        if (path.startsWith("/auth/")) {
            if (!rateLimiter.isAllowed(clientIp)) {
                logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                
                long retryAfter = rateLimiter.getBlockedSecondsRemaining(clientIp);
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                
                response.getWriter().write(
                    "{\"error\":\"rate_limit_exceeded\"," +
                    "\"message\":\"Too many requests. Please try again later.\"," +
                    "\"retryAfterSeconds\":" + retryAfter + "}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the real client IP, considering proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
