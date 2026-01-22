package com.example.lab10.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

  private final Key key;
  private final long expMillis;

  public JwtUtil(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.exp-minutes}") long expMinutes
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expMillis = expMinutes * 60_000L;
  }

  public String generateToken(String username, Long userId, List<String> roles) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expMillis);

    return Jwts.builder()
        .subject(username)
        .claims(Map.of(
            "userId", userId,
            "roles", roles
        ))
        .issuedAt(now)
        .expiration(exp)
        .signWith(key)
        .compact();
  }

  public String generateToken(String username, List<String> roles) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expMillis);

    return Jwts.builder()
        .subject(username)
        .claims(Map.of("roles", roles))
        .issuedAt(now)
        .expiration(exp)
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith((javax.crypto.SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String getUsername(String token) {
    return parse(token).getSubject();
  }

  public Long getUserId(String token) {
    Object userId = parse(token).get("userId");
    if (userId instanceof Number) {
      return ((Number) userId).longValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<String> getRoles(String token) {
    return (List<String>) parse(token).get("roles", List.class);
  }

  public boolean isTokenExpired(String token) {
    return parse(token).getExpiration().before(new Date());
  }
}
