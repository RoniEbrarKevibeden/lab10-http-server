package com.example.lab10.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to add HSTS (HTTP Strict Transport Security) header.
 * Only active when HTTPS profile is enabled.
 * 
 * HSTS instructs browsers to:
 * - Only access the site via HTTPS
 * - Automatically convert http:// requests to https://
 * - Refuse to connect if certificate is invalid
 */
@Component
@Profile("https")
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class HstsFilter extends OncePerRequestFilter {

    // max-age in seconds (1 year = 31536000 seconds)
    @Value("${security.hsts.max-age:31536000}")
    private long maxAge;

    @Value("${security.hsts.include-subdomains:true}")
    private boolean includeSubDomains;

    @Value("${security.hsts.preload:false}")
    private boolean preload;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only add HSTS header for HTTPS requests
        if (request.isSecure()) {
            StringBuilder headerValue = new StringBuilder();
            headerValue.append("max-age=").append(maxAge);
            
            if (includeSubDomains) {
                headerValue.append("; includeSubDomains");
            }
            
            if (preload) {
                headerValue.append("; preload");
            }
            
            response.setHeader("Strict-Transport-Security", headerValue.toString());
        }

        filterChain.doFilter(request, response);
    }
}
