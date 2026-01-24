package com.optic.console.application.service;

import com.optic.console.application.TokenGenerator;
import com.optic.console.domain.auth.TokenType;
import com.optic.console.domain.auth.VerificationToken;
import com.optic.console.domain.auth.VerificationTokenRepository;
import com.optic.console.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    private User testUser;
    private final String testToken = "test-token-123";
    private final TokenType testTokenType = TokenType.PASSWORD_RESET;
    private final Duration testValidity = Duration.ofHours(1);

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void createToken_NewToken_SavesAndReturnsToken() {
        when(tokenGenerator.generate(32)).thenReturn(testToken);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result = verificationTokenService.createToken(testUser, testTokenType, testValidity);

        assertNotNull(result);
        assertEquals(testToken, result.getToken());
        assertEquals(testUser, result.getUser());
        assertEquals(testTokenType, result.getType());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));
        assertNull(result.getUsedAt());

        verify(verificationTokenRepository).findByUserAndTypeAndUsedAtIsNull(testUser, testTokenType);
        verify(verificationTokenRepository, atLeastOnce()).save(any(VerificationToken.class));
    }

    @Test
    void createToken_ExistingActiveToken_MarksExistingAsUsed() {
        VerificationToken existingToken = new VerificationToken();
        existingToken.setId(1L);
        existingToken.setToken("old-token");
        existingToken.setUser(testUser);
        existingToken.setType(testTokenType);
        
        when(verificationTokenRepository.findByUserAndTypeAndUsedAtIsNull(testUser, testTokenType))
            .thenReturn(Optional.of(existingToken));
        when(tokenGenerator.generate(32)).thenReturn(testToken);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result = verificationTokenService.createToken(testUser, testTokenType, testValidity);

        assertNotNull(result);
        assertNotNull(existingToken.getUsedAt()); // Old token should be marked as used
        assertNotEquals(existingToken.getToken(), result.getToken()); // Should be a new token
        verify(verificationTokenRepository).save(existingToken); // Verify old token was updated
    }

    @Test
    void isValidToken_ValidToken_ReturnsTrueAndMarksAsUsed() {
        VerificationToken token = new VerificationToken();
        token.setToken(testToken);
        token.setType(testTokenType);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(verificationTokenRepository.findByTokenAndType(testToken, testTokenType))
            .thenReturn(Optional.of(token));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(token);

        boolean isValid = verificationTokenService.isValidToken(testToken, testTokenType);

        assertTrue(isValid);
        assertNotNull(token.getUsedAt());
        verify(verificationTokenRepository).save(token);
    }

    @Test
    void isValidToken_ExpiredToken_ReturnsFalse() {
        VerificationToken token = new VerificationToken();
        token.setToken(testToken);
        token.setType(testTokenType);
        token.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        when(verificationTokenRepository.findByTokenAndType(testToken, testTokenType))
            .thenReturn(Optional.of(token));

        boolean isValid = verificationTokenService.isValidToken(testToken, testTokenType);

        assertFalse(isValid);
        assertNull(token.getUsedAt());
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void isValidToken_AlreadyUsed_ReturnsFalse() {
        VerificationToken token = new VerificationToken();
        token.setToken(testToken);
        token.setType(testTokenType);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.markAsUsed();
        
        when(verificationTokenRepository.findByTokenAndType(testToken, testTokenType))
            .thenReturn(Optional.of(token));

        boolean isValid = verificationTokenService.isValidToken(testToken, testTokenType);

        assertFalse(isValid);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void isValidToken_NonExistentToken_ReturnsFalse() {
        when(verificationTokenRepository.findByTokenAndType("nonexistent", testTokenType))
            .thenReturn(Optional.empty());

        boolean isValid = verificationTokenService.isValidToken("nonexistent", testTokenType);

        assertFalse(isValid);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void cleanupExpiredTokens_DeletesOldTokens() {
        verificationTokenService.cleanupExpiredTokens();

        verify(verificationTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void isValidToken_NullTokenString_ReturnsFalse() {
        when(verificationTokenRepository.findByTokenAndType(null, testTokenType))
            .thenReturn(Optional.empty());

        boolean isValid = verificationTokenService.isValidToken(null, testTokenType);

        assertFalse(isValid);
        verify(verificationTokenRepository).findByTokenAndType(null, testTokenType);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void isValidToken_NullTokenType_ReturnsFalse() {
        when(verificationTokenRepository.findByTokenAndType(testToken, null))
            .thenReturn(Optional.empty());

        boolean isValid = verificationTokenService.isValidToken(testToken, null);

        assertFalse(isValid);
        verify(verificationTokenRepository).findByTokenAndType(testToken, null);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void isValidToken_ExpiredButNotUsed_ReturnsFalse() {
        VerificationToken token = new VerificationToken();
        token.setToken(testToken);
        token.setType(testTokenType);
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1)); // Just expired
        
        when(verificationTokenRepository.findByTokenAndType(testToken, testTokenType))
            .thenReturn(Optional.of(token));

        boolean isValid = verificationTokenService.isValidToken(testToken, testTokenType);

        assertFalse(isValid);
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void createToken_WithVeryShortValidity_SetsCorrectExpiration() {
        Duration shortValidity = Duration.ofMinutes(5);
        when(tokenGenerator.generate(32)).thenReturn(testToken);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result = verificationTokenService.createToken(testUser, testTokenType, shortValidity);

        assertNotNull(result);
        LocalDateTime expectedMin = LocalDateTime.now().plusMinutes(4);
        LocalDateTime expectedMax = LocalDateTime.now().plusMinutes(6);
        assertTrue(result.getExpiresAt().isAfter(expectedMin) && result.getExpiresAt().isBefore(expectedMax),
                "Expiration should be approximately 5 minutes from now");
    }

    @Test
    void createToken_WithVeryLongValidity_SetsCorrectExpiration() {
        // Given
        Duration longValidity = Duration.ofDays(30);
        when(tokenGenerator.generate(32)).thenReturn(testToken);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerificationToken result = verificationTokenService.createToken(testUser, testTokenType, longValidity);

        assertNotNull(result);
        LocalDateTime expectedMin = LocalDateTime.now().plusDays(29);
        LocalDateTime expectedMax = LocalDateTime.now().plusDays(31);
        assertTrue(result.getExpiresAt().isAfter(expectedMin) && result.getExpiresAt().isBefore(expectedMax),
                "Expiration should be approximately 30 days from now");
    }
}
