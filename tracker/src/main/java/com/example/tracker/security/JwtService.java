package com.example.tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "app.jwt.secret must be base64-encoded. Generate one with: openssl rand -base64 32", e);
        }
        try {
            this.key = Keys.hmacShaKeyFor(decoded);
        } catch (WeakKeyException e) {
            throw new IllegalStateException(
                "app.jwt.secret decodes to " + decoded.length
                    + " bytes; HS256 requires at least 32. Generate one with: openssl rand -base64 32", e);
        }
        this.expirationMs = expirationMs;
    }

    public String generateToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies the signature and expiry, then returns the subject.
     * Throws JwtException for a tampered, malformed or expired token.
     */
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
