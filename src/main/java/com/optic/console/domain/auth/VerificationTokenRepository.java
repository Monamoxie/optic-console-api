package com.optic.console.domain.auth;

import com.optic.console.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);
    boolean existsByUserAndTypeAndUsedAtIsNull(User user, TokenType type);
    void deleteByExpiresAtBefore(LocalDateTime date);
}