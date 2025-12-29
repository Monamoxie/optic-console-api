package com.optic.console.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class TokenGenerator {
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public String generate(int bytes) {
        byte[] buffer = new byte[bytes];
        random.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }
}
