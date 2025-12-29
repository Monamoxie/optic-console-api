package com.optic.console.application.service;

import com.optic.console.application.TokenGenerator;
import com.optic.console.domain.auth.TokenType;
import com.optic.console.domain.auth.VerificationToken;
import com.optic.console.domain.auth.VerificationTokenRepository;
import com.optic.console.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public VerificationToken createToken(User user, TokenType type, Duration validity) {
        verificationTokenRepository.findByUserAndTypeAndUsedAtIsNull(user, type)
                .ifPresent(token -> {
                    token.markAsUsed();
                    verificationTokenRepository.save(token);
                });

        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(type);
        token.setToken(tokenGenerator.generate(32));
        token.setExpiresAt(LocalDateTime.now().plus(validity));

        return verificationTokenRepository.save(token);
    }

    @Transactional
    public boolean validateToken(String tokenString, TokenType type) {
        return verificationTokenRepository.findByTokenAndType(tokenString, type)
                .map(token -> {
                    if (token.isExpired() || token.isUsed()) {
                        return false;
                    }
                    token.markAsUsed();
                    verificationTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now().minusDays(7));
    }
}