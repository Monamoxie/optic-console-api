package com.optic.console.application.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String name, String resetLink);
}