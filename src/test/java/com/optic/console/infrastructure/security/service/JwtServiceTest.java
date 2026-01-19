package com.optic.console.infrastructure.security.service;

import java.util.Date;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.optic.console.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_EMAIL = "test@example.com";
    // Test secret must be at least 32 bytes for HS256
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long-for-hs256-algorithm";
    private static final SecretKey TEST_KEY = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    private static final long TEST_EXPIRATION = 86400000L; // 1 day

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setExpirationMillis(String.valueOf(TEST_EXPIRATION));
        jwtService = new JwtService(jwtProperties);
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

    @Test
    void generateToken_WhitespaceOnlyEmail_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtService.generateToken("   ");
        }, "Should throw exception for whitespace-only email");
    }

    @Test
    void generateToken_ExpirationIsOneDay() {
        // Given
        long oneDayInMillis = 86400000L;
        String token = jwtService.generateToken(TEST_EMAIL);

        // When
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(TEST_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Then
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        long actualExpiration = expiration.getTime() - issuedAt.getTime();
        
        // Allow 1 second tolerance for execution time
        assertTrue(Math.abs(actualExpiration - oneDayInMillis) < 1000, 
                "Token expiration should be approximately 1 day (86400000 ms)");
    }

    @Test
    void generateToken_SameEmailMultipleTimes_GeneratesValidTokensWithSameSubject() {
        // Generate multiple tokens for the same email
        String token1 = jwtService.generateToken(TEST_EMAIL);
        // Ensure different timestamp by waiting at least 1 second (JWT uses seconds precision)
        try {
            Thread.sleep(1100); // Wait 1.1 seconds to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtService.generateToken(TEST_EMAIL);

        // Both tokens should be valid and have the same subject
        Claims claims1 = Jwts.parserBuilder().setSigningKey(TEST_KEY).build()
                .parseClaimsJws(token1).getBody();
        Claims claims2 = Jwts.parserBuilder().setSigningKey(TEST_KEY).build()
                .parseClaimsJws(token2).getBody();
        
        assertEquals(claims1.getSubject(), claims2.getSubject(), "Both tokens should have same subject");
        assertEquals(TEST_EMAIL, claims1.getSubject(), "Token 1 subject should match email");
        assertEquals(TEST_EMAIL, claims2.getSubject(), "Token 2 subject should match email");
        
        // Tokens should have different issuedAt times (unless generated in the same second)
        // Note: JWT timestamps are in seconds, so tokens generated in the same second will be identical
        // This is acceptable behavior - the important thing is they're both valid and have correct subject
        if (!claims1.getIssuedAt().equals(claims2.getIssuedAt())) {
            assertNotEquals(token1, token2, "Tokens with different issuedAt should be different");
        }
        // If issuedAt is the same, tokens will be identical, which is acceptable
    }

    @Test
    void constructor_EmptySecret_ThrowsException() {
        // Given
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("");
        jwtProperties.setExpirationMillis(String.valueOf(TEST_EXPIRATION));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            new JwtService(jwtProperties);
        }, "Should throw exception for empty secret");
    }

    @Test
    void constructor_NullSecret_ThrowsException() {
        // Given
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(null);
        jwtProperties.setExpirationMillis(String.valueOf(TEST_EXPIRATION));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            new JwtService(jwtProperties);
        }, "Should throw exception for null secret");
    }

    @Test
    void constructor_ShortSecret_ThrowsException() {
        // Given - secret shorter than 32 bytes
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("tooshort");
        jwtProperties.setExpirationMillis(String.valueOf(TEST_EXPIRATION));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new JwtService(jwtProperties);
        }, "Should throw exception for secret shorter than 32 bytes");
        assertTrue(exception.getMessage().contains("too short"));
    }
}
