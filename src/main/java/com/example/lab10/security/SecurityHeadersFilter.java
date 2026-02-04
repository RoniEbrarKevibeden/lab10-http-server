package com.example.lab10.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to add security headers to all responses.
 * These headers instruct browsers to enforce extra security rules.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // X-Content-Type-Options: Prevents MIME type sniffing
        // Protects against script injection attacks
        response.setHeader("X-Content-Type-Options", "nosniff");

        // X-Frame-Options: Prevents clickjacking attacks
        // DENY = page cannot be displayed in a frame
        response.setHeader("X-Frame-Options", "DENY");

        // Content-Security-Policy: Controls resource loading
        // This is a basic policy - adjust based on your app needs
        response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; " +
                "font-src 'self'; " +
                "frame-ancestors 'none'; " +
                "form-action 'self'");

        // Referrer-Policy: Controls how much referrer info is sent
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // X-XSS-Protection: Legacy XSS protection (for older browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Permissions-Policy: Restricts browser features
        response.setHeader("Permissions-Policy", 
                "geolocation=(), microphone=(), camera=()");

        // Cache-Control: Prevent caching of sensitive data
        if (request.getRequestURI().startsWith("/auth/") || 
            request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
        }

        filterChain.doFilter(request, response);
    }
}
