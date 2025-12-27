package com.optic.console.infrastructure.email;

public interface EmailService {
    void sendPasswordResetEmail(String to, String name, String resetLink);
}