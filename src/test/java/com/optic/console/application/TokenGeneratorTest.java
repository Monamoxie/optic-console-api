package com.optic.console.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenGeneratorTest {
    @Mock
    private SecureRandom mockRandom;

    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenGenerator = new TokenGenerator(mockRandom);
    }

    @Test
    void generate_ReturnsBase64UrlEncodedString() {
        // Given
        byte[] testBytes = {1, 2, 3, 4, 5, 6, 7, 8};
        when(mockRandom.nextBytes(any())).thenAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            System.arraycopy(testBytes, 0, arg, 0, testBytes.length);
            return null;
        });

        // When
        String result = tokenGenerator.generate(8);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("+") && !result.contains("/"),
                "Should use URL-safe Base64 encoding");
        assertFalse(result.endsWith("="), "Should use withoutPadding");
    }

    @Test
    void generate_WithDifferentLengths_ReturnsCorrectLength() {

        int[] lengths = {16, 24, 32};

        for (int len : lengths) {

            String result = tokenGenerator.generate(len);


            int expectedMinLength = (int) Math.ceil(len * 4 / 3.0) - 2;
            assertTrue(result.length() >= expectedMinLength,
                    "Token length should be at least " + expectedMinLength);
        }
    }

    @Test
    void generate_ProducesDifferentResults() {

        String token1 = tokenGenerator.generate(32);
        String token2 = tokenGenerator.generate(32);


        assertNotEquals(token1, token2, "Subsequent calls should produce different tokens");
    }

    @Test
    void generate_WithZeroLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            tokenGenerator.generate(0);
        }, "Should throw exception for zero length");
    }

    @Test
    void generate_WithNegativeLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            tokenGenerator.generate(-1);
        }, "Should throw exception for negative length");
    }
}