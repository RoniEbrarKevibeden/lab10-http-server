package com.example.lab10.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

/**
 * Listener for security events (login success/failure, authorization denied).
 * Logs security-relevant events without exposing sensitive data.
 */
@Component
public class SecurityEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SecurityEventListener.class);

    /**
     * Logs failed authentication attempts.
     * IMPORTANT: Never log passwords or tokens!
     */
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String failureType = event.getException().getClass().getSimpleName();
        
        // Log without sensitive data - no password, no token
        logger.warn("SECURITY: Failed login attempt - username: {}, reason: {}", 
                maskUsername(username), failureType);
    }

    /**
     * Logs successful authentication.
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        
        logger.info("SECURITY: Successful login - username: {}", maskUsername(username));
    }

    /**
     * Logs authorization denied events (403 Forbidden).
     */
    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        Object principal = event.getAuthentication().get().getPrincipal();
        String username = principal != null ? principal.toString() : "anonymous";
        
        logger.warn("SECURITY: Unauthorized access attempt - user: {}", 
                maskUsername(username));
    }

    /**
     * Masks username for privacy in logs.
     * Shows first 2 characters, masks the rest.
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        return username.substring(0, 2) + "***";
    }
}
