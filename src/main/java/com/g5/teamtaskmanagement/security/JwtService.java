package com.g5.teamtaskmanagement.security;

import com.g5.teamtaskmanagement.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMillis;

    public JwtService(@Value("${jwt.secret:change-me-please}") String secret,
            @Value("${jwt.access-token-ms:3600000}") long accessTokenMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMillis = accessTokenMillis;
    }

    public String generateAccessToken(User user) {
        return generateAccessToken(user, null);
    }

    public String generateAccessToken(User user, Long sessionId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date exp = new Date(now + accessTokenMillis);
        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .setIssuedAt(issuedAt)
                .setExpiration(exp);
        if (sessionId != null) {
            builder.claim("sid", sessionId);
        }
        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
