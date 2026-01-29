package com.example.lab10.integration;

import com.example.lab10.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for access control via JWT.
 * Tests role-based claims in JWT tokens.
 */
@DisplayName("Access Control Tests")
class AdminAccessIntegrationTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hs256";
    private static final long EXP_MINUTES = 15;
    
    private final JwtUtil jwtUtil = new JwtUtil(SECRET, EXP_MINUTES);

    @Test
    @DisplayName("Token with ROLE_USER should contain user role")
    void token_WithUserRole_ShouldContainUserRole() {
        String token = jwtUtil.generateToken("testuser", List.of("ROLE_USER"));
        var claims = jwtUtil.parse(token);
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        
        assertTrue(roles.contains("ROLE_USER"));
        assertFalse(roles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Token with ROLE_ADMIN should contain admin role")
    void token_WithAdminRole_ShouldContainAdminRole() {
        String token = jwtUtil.generateToken("admin", List.of("ROLE_USER", "ROLE_ADMIN"));
        var claims = jwtUtil.parse(token);
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Different users should have different tokens")
    void differentUsers_ShouldHaveDifferentTokens() {
        String userToken = jwtUtil.generateToken("user", List.of("ROLE_USER"));
        String adminToken = jwtUtil.generateToken("admin", List.of("ROLE_ADMIN"));
        
        assertNotEquals(userToken, adminToken);
    }

    @Test
    @DisplayName("Token subject should match username")
    void tokenSubject_ShouldMatchUsername() {
        String token = jwtUtil.generateToken("myuser", List.of("ROLE_USER"));
        var claims = jwtUtil.parse(token);
        
        assertEquals("myuser", claims.getSubject());
    }
}
