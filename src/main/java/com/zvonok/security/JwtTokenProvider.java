package com.zvonok.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.ExpirationMs}")
    private long jwtExpirationMs;

    // ===== УЛУЧШЕННАЯ ВЕРСИЯ С USERID =====

    public String generateToken(String username, Long userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId) // Добавляем userId в токен
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public boolean isValidToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Неверный JWT токен: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        }
        if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        }
        return null;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
