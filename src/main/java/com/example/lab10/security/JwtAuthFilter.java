package com.example.lab10.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String JWT_COOKIE_NAME = "jwt_token";

  public JwtAuthFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null) {
      try {
        Claims claims = jwtUtil.parse(token);
        String username = claims.getSubject();

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles", List.class);
        var authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();

        var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        
        Object userId = claims.get("userId");
        if (userId != null) {
          auth.setDetails(userId);
        }

        SecurityContextHolder.getContext().setAuthentication(auth);

      } catch (ExpiredJwtException e) {
        logger.debug("JWT token has expired");
      } catch (JwtException e) {
        logger.debug("Invalid JWT token");
      } catch (Exception e) {
        logger.debug("Could not process JWT token");
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (JWT_COOKIE_NAME.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
