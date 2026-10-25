package com.example.tracker.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET_A =
        Base64.getEncoder().encodeToString("secret-a-which-is-32-bytes-long!".getBytes());
    private static final String SECRET_B =
        Base64.getEncoder().encodeToString("secret-b-which-is-32-bytes-long!".getBytes());

    private final JwtService jwtService = new JwtService(SECRET_A, 3600000L);

    @Test
    void roundTripsTheSubject() {
        String token = jwtService.generateToken("a@b.com");
        assertEquals("a@b.com", jwtService.extractEmail(token));
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        String foreignToken = new JwtService(SECRET_B, 3600000L).generateToken("a@b.com");
        assertThrows(JwtException.class, () -> jwtService.extractEmail(foreignToken));
    }

    @Test
    void rejectsATamperedPayload() {
        String token = jwtService.generateToken("a@b.com");

        // Swap one character of the payload segment; the signature no longer matches
        String[] parts = token.split("[.]");
        char[] payload = parts[1].toCharArray();
        payload[0] = payload[0] == 'a' ? 'b' : 'a';
        String tampered = parts[0] + "." + new String(payload) + "." + parts[2];

        assertThrows(JwtException.class, () -> jwtService.extractEmail(tampered));
    }

    @Test
    void rejectsAnExpiredToken() {
        // Negative lifetime produces a token that expired before it was issued
        String expired = new JwtService(SECRET_A, -1000L).generateToken("a@b.com");
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(expired));
    }

    @Test
    void rejectsASecretShorterThanHs256Requires() {
        String tooShort = Base64.getEncoder().encodeToString("only-16-bytes!!!".getBytes());
        assertThrows(IllegalStateException.class, () -> new JwtService(tooShort, 3600000L));
    }
}
