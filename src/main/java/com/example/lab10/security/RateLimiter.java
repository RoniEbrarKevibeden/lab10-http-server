package com.example.lab10.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter to prevent brute-force attacks.
 * Limits the number of requests per IP address within a time window.
 */
@Component
public class RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    // Configuration
    private static final int MAX_REQUESTS_PER_WINDOW = 100; // Max requests
    private static final long WINDOW_SIZE_MS = 60_000;     // 1 minute window
    private static final long BLOCK_DURATION_MS = 60_000; // 1 minute block

    // Storage for request counts and block times
    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedIps = new ConcurrentHashMap<>();

    /**
     * Checks if a request from the given IP should be allowed.
     * Returns true if allowed, false if rate limited.
     */
    public boolean isAllowed(String ipAddress) {
        // Check if IP is blocked
        Long blockedUntil = blockedIps.get(ipAddress);
        if (blockedUntil != null) {
            if (System.currentTimeMillis() < blockedUntil) {
                logger.warn("Rate limit: IP {} is blocked until {}", ipAddress, blockedUntil);
                return false;
            } else {
                // Block expired, remove it
                blockedIps.remove(ipAddress);
                requestCounts.remove(ipAddress);
            }
        }

        // Get or create request info for this IP
        RequestInfo info = requestCounts.compute(ipAddress, (key, existing) -> {
            long now = System.currentTimeMillis();

            if (existing == null || now - existing.windowStart > WINDOW_SIZE_MS) {
                // Start new window
                return new RequestInfo(now, new AtomicInteger(1));
            } else {
                // Increment counter in current window
                existing.count.incrementAndGet();
                return existing;
            }
        });

        // Check if limit exceeded
        if (info.count.get() > MAX_REQUESTS_PER_WINDOW) {
            // Block the IP
            blockedIps.put(ipAddress, System.currentTimeMillis() + BLOCK_DURATION_MS);
            logger.warn("Rate limit exceeded: IP {} blocked for {} minutes", 
                    ipAddress, BLOCK_DURATION_MS / 60000);
            return false;
        }

        return true;
    }

    /**
     * Records a failed login attempt for stricter rate limiting.
     */
    public void recordFailedLogin(String ipAddress) {
        RequestInfo info = requestCounts.get(ipAddress);
        if (info != null) {
            // Count failed logins as 3 requests (more aggressive limiting)
            info.count.addAndGet(2);
        }
        logger.warn("Failed login attempt recorded for IP: {}", ipAddress);
    }

    /**
     * Resets the rate limit for an IP (e.g., after successful login).
     */
    public void resetLimit(String ipAddress) {
        requestCounts.remove(ipAddress);
        blockedIps.remove(ipAddress);
    }

    /**
     * Checks if an IP is currently blocked.
     */
    public boolean isBlocked(String ipAddress) {
        Long blockedUntil = blockedIps.get(ipAddress);
        if (blockedUntil != null && System.currentTimeMillis() < blockedUntil) {
            return true;
        }
        return false;
    }

    /**
     * Gets remaining time until IP is unblocked (in seconds).
     */
    public long getBlockedSecondsRemaining(String ipAddress) {
        Long blockedUntil = blockedIps.get(ipAddress);
        if (blockedUntil == null) return 0;
        long remaining = blockedUntil - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }

    private static class RequestInfo {
        final long windowStart;
        final AtomicInteger count;

        RequestInfo(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
