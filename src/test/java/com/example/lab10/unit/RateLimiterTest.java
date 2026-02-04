package com.example.lab10.unit;

import com.example.lab10.security.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimiter.
 */
@DisplayName("RateLimiter Unit Tests")
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    @DisplayName("First request should be allowed")
    void firstRequest_ShouldBeAllowed() {
        assertTrue(rateLimiter.isAllowed("192.168.1.1"));
    }

    @Test
    @DisplayName("Multiple requests from same IP should be allowed within limit")
    void multipleRequests_ShouldBeAllowedWithinLimit() {
        String ip = "192.168.1.2";
        
        for (int i = 0; i < 50; i++) {
            assertTrue(rateLimiter.isAllowed(ip), "Request " + i + " should be allowed");
        }
    }

    @Test
    @DisplayName("Different IPs should have separate limits")
    void differentIPs_ShouldHaveSeparateLimits() {
        String ip1 = "192.168.1.3";
        String ip2 = "192.168.1.4";
        
        // Both should be allowed
        assertTrue(rateLimiter.isAllowed(ip1));
        assertTrue(rateLimiter.isAllowed(ip2));
    }

    @Test
    @DisplayName("Blocked IP check should work")
    void blockedIPCheck_ShouldWork() {
        String ip = "192.168.1.5";
        
        // Initially not blocked
        assertFalse(rateLimiter.isBlocked(ip));
    }
}
