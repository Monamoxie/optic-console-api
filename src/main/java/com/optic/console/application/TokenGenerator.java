package com.optic.console.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TokenGenerator {
    private final SecureRandom random;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public TokenGenerator() {
        this(new SecureRandom());
    }

    protected TokenGenerator(SecureRandom secureRandom) {
        this.random = secureRandom;
    }

    protected SecureRandom createSecureRandom() {
        return new SecureRandom();
    }

    public String generate(int bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("Number of bytes must be positive");
        }
        
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }
}
