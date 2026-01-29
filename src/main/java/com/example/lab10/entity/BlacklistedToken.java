package com.example.lab10.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity for storing blacklisted/invalidated JWT tokens.
 * When a user logs out, their access token is added here to prevent reuse.
 */
@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private Instant blacklistedAt;

    @Column(nullable = false)
    private Instant expiryDate;

    public BlacklistedToken() {}

    public BlacklistedToken(String token, Instant expiryDate) {
        this.token = token;
        this.blacklistedAt = Instant.now();
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getBlacklistedAt() { return blacklistedAt; }
    public void setBlacklistedAt(Instant blacklistedAt) { this.blacklistedAt = blacklistedAt; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}
