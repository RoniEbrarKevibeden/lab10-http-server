package com.example.lab10.service;

import com.example.lab10.entity.BlacklistedToken;
import com.example.lab10.entity.RefreshToken;
import com.example.lab10.repo.BlacklistedTokenRepository;
import com.example.lab10.repo.RefreshTokenRepository;
import com.example.lab10.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing JWT tokens and refresh tokens.
 * Implements token rotation, blacklisting, and expiration handling.
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-exp-days:7}")
    private long refreshTokenExpDays;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        BlacklistedTokenRepository blacklistedTokenRepository,
                        JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Creates a new refresh token for the given username.
     * Any existing refresh tokens for the user are revoked (rotation).
     */
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        // Revoke all existing refresh tokens for this user (rotation)
        refreshTokenRepository.revokeAllByUsername(username);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                username,
                Instant.now().plusSeconds(refreshTokenExpDays * 24 * 60 * 60)
        );

        refreshToken = refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token created for user: {}", username);
        return refreshToken;
    }

    /**
     * Validates and rotates a refresh token.
     * Returns a new refresh token if the old one is valid.
     */
    @Transactional
    public Optional<RefreshToken> rotateRefreshToken(String token) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenAndRevokedFalse(token);

        if (tokenOpt.isEmpty()) {
            logger.warn("Refresh token not found or already revoked");
            return Optional.empty();
        }

        RefreshToken refreshToken = tokenOpt.get();

        if (refreshToken.isExpired()) {
            logger.warn("Refresh token expired for user: {}", refreshToken.getUsername());
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            return Optional.empty();
        }

        // Revoke old token (rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Create new token
        RefreshToken newToken = new RefreshToken(
                UUID.randomUUID().toString(),
                refreshToken.getUsername(),
                Instant.now().plusSeconds(refreshTokenExpDays * 24 * 60 * 60)
        );

        newToken = refreshTokenRepository.save(newToken);
        logger.info("Refresh token rotated for user: {}", refreshToken.getUsername());
        return Optional.of(newToken);
    }

    /**
     * Validates a refresh token without rotating it.
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenAndRevokedFalse(token);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken refreshToken = tokenOpt.get();
        if (refreshToken.isExpired()) {
            return Optional.empty();
        }

        return tokenOpt;
    }

    /**
     * Blacklists an access token (used during logout).
     */
    @Transactional
    public void blacklistAccessToken(String token) {
        try {
            Claims claims = jwtUtil.parse(token);
            Instant expiry = claims.getExpiration().toInstant();

            if (!blacklistedTokenRepository.existsByToken(token)) {
                BlacklistedToken blacklisted = new BlacklistedToken(token, expiry);
                blacklistedTokenRepository.save(blacklisted);
                logger.info("Access token blacklisted for user: {}", claims.getSubject());
            }
        } catch (Exception e) {
            logger.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    /**
     * Checks if an access token is blacklisted.
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    /**
     * Revokes all tokens for a user (used during logout).
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        refreshTokenRepository.revokeAllByUsername(username);
        logger.info("All refresh tokens revoked for user: {}", username);
    }

    /**
     * Scheduled task to clean up expired tokens from the database.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        blacklistedTokenRepository.deleteExpiredTokens(now);
        logger.debug("Expired tokens cleaned up");
    }
}
