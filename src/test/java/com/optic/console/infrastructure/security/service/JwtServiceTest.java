package com.optic.console.infrastructure.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_EMAIL = "test@example.com";
    private static final Key TEST_KEY = Keys.hmacShaKeyFor("supersecretkeythatshouldbereplacedwithenv".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ValidEmail_ReturnsValidJwtToken() {
        String token = jwtService.generateToken(TEST_EMAIL);
        

        assertNotNull(token, "Token should not be null");
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        assertEquals(TEST_EMAIL, claims.getSubject(), "Token subject should match email");
        assertNotNull(claims.getIssuedAt(), "Token should have issued at time");
        assertNotNull(claims.getExpiration(), "Token should have expiration time");
        
        // Verify expiration is in the future
        assertTrue(claims.getExpiration().after(new Date()), "Token expiration should be in the future");
    }

    @Test
    void generateToken_DifferentEmails_GenerateDifferentTokens() {
        String token1 = jwtService.generateToken("user1@example.com");
        String token2 = jwtService.generateToken("user2@example.com");

        assertNotEquals(token1, token2, "Tokens for different users should be different");
    }

    @Test
    void generateToken_EmptyEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateToken("");
        }, "Should throw exception for empty email");
    }

    @Test
    void generateToken_NullEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateToken(null);
        }, "Should throw exception for null email");
    }
}
