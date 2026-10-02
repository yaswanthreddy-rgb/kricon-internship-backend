package com.kricontech.interviewace.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static final String SECRET = "test-jwt-secret-with-sufficient-key-material";

    @Test
    void generatedTokenContainsEmailAndIsValid() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);
        String token = jwtUtil.generateToken("casey@example.com");

        assertTrue(jwtUtil.isValid(token));
        assertEquals("casey@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void rejectsMalformedAndExpiredTokens() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);
        JwtUtil expiredTokenGenerator = new JwtUtil(SECRET, -1);

        assertFalse(jwtUtil.isValid("not-a-jwt"));
        assertFalse(jwtUtil.isValid(expiredTokenGenerator.generateToken("casey@example.com")));
    }

    @Test
    void rejectsTokensSignedByAnotherSecret() {
        JwtUtil issuer = new JwtUtil(SECRET, 60_000);
        JwtUtil validator = new JwtUtil("another-distinct-test-secret-value", 60_000);

        assertFalse(validator.isValid(issuer.generateToken("casey@example.com")));
    }
}