package com.optic.console.infrastructure.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.optic.console.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {
    
    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(JwtProperties jwtProperties) {
        if (jwtProperties == null || jwtProperties.getSecret() == null || jwtProperties.getSecret().trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret is not configured. Set JWT_SECRET environment variable or jwt.secret property. " +
                "Secret must be at least 32 characters (256 bits) for HS256 algorithm."
            );
        }

        String jwtSecret = jwtProperties.getSecret();

        // Validate secret length - HS256 requires at least 256 bits (32 bytes)
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                String.format(
                    "JWT secret is too short: %d bytes. HS256 requires at least 32 bytes (256 bits). " +
                    "Current secret length is insufficient for secure token signing.",
                    secretBytes.length
                )
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(secretBytes);

        long expiration;
        String expirationStr = jwtProperties.getExpirationMillis();
        if (expirationStr == null || expirationStr.trim().isEmpty()) {
            expiration = 86400000L;
        } else {
            try {
                expiration = Long.parseLong(expirationStr.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid JWT expiration value: {}. Using default: 86400000 ms", expirationStr);
                expiration = 86400000L;
            }
        }
        this.expirationMillis = expiration;
        
        log.info("JWT service initialized with expiration: {} ms ({} hours)", 
                expirationMillis, expirationMillis / 3600000.0);
    }


    public String generateToken(String subject, Boolean rememberMe) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }

        long effectiveExpiration = Boolean.TRUE.equals(rememberMe)
                ? expirationMillis * 7
                : expirationMillis;

        Date now = new Date();

        Date expiration = new Date(now.getTime() + effectiveExpiration);

        return Jwts.builder()
                .setSubject(subject.trim())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}

