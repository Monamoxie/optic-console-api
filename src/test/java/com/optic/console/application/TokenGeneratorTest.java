package com.optic.console.application;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import org.mockito.junit.jupiter.MockitoExtension;

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
        byte[] testBytes = {1, 2, 3, 4, 5, 6, 7, 8};
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            System.arraycopy(testBytes, 0, arg, 0, testBytes.length);
            return null;
        }).when(mockRandom).nextBytes(any());

        String result = tokenGenerator.generate(8);

        assertNotNull(result);
        assertFalse(result.contains("+"), "Should use URL-safe Base64 encoding (no +)");
        assertFalse(result.contains("/"), "Should use URL-safe Base64 encoding (no /)");
        assertFalse(result.endsWith("="), "Should use withoutPadding");

        java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
        byte[] decoded = decoder.decode(result);
        assertTrue(decoded.length == 8, "Decoded length should match input bytes");
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
        byte[] firstBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                             17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        byte[] secondBytes = {33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
                              49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64};
        
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            System.arraycopy(firstBytes, 0, arg, 0, Math.min(firstBytes.length, arg.length));
            return null;
        }).doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            System.arraycopy(secondBytes, 0, arg, 0, Math.min(secondBytes.length, arg.length));
            return null;
        }).when(mockRandom).nextBytes(any());

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

    @Test
    void generate_WithVeryLargeSize_StillWorks() {
        int largeSize = 1024;
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(0);
            for (int i = 0; i < arg.length; i++) {
                arg[i] = (byte) (i % 256);
            }
            return null;
        }).when(mockRandom).nextBytes(any());

        String result = tokenGenerator.generate(largeSize);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
        byte[] decoded = decoder.decode(result);
        assertTrue(decoded.length >= largeSize * 3 / 4 - 2, "Decoded size should be approximately correct");
    }
}