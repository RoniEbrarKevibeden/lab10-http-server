package com.example.lab10.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * Tests: token generation, parsing, claims extraction.
 */
@DisplayName("JWT Util Tests")
class JwtUtilTest {

    // 256-bit secret for HS256
    private static final String SECRET = "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hs256";
    private static final long EXP_MINUTES = 15;
    
    private final JwtUtil jwtUtil = new JwtUtil(SECRET, EXP_MINUTES);

    private static final String TEST_USER = "testuser";

    @Test
    @DisplayName("Generate token should return non-empty string")
    void generateToken_ShouldReturnNonEmptyString() {
        String token = jwtUtil.generateToken(TEST_USER, List.of("ROLE_USER"));

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Token should contain correct username in subject")
    void token_ShouldContainCorrectUsername() {
        String token = jwtUtil.generateToken(TEST_USER, List.of("ROLE_USER"));
        
        var claims = jwtUtil.parse(token);
        
        assertEquals(TEST_USER, claims.getSubject());
    }

    @Test
    @DisplayName("Token should contain roles claim")
    void token_ShouldContainRoles() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = jwtUtil.generateToken(TEST_USER, roles);
        
        var claims = jwtUtil.parse(token);
        
        assertNotNull(claims.get("roles"));
    }

    @Test
    @DisplayName("Token should have expiration date")
    void token_ShouldHaveExpiration() {
        String token = jwtUtil.generateToken(TEST_USER, List.of("ROLE_USER"));
        
        var claims = jwtUtil.parse(token);
        
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    @DisplayName("Different users should get different tokens")
    void differentUsers_ShouldGetDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1", List.of("ROLE_USER"));
        String token2 = jwtUtil.generateToken("user2", List.of("ROLE_USER"));

        assertNotEquals(token1, token2);
    }
}
