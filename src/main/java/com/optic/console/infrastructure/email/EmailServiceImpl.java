package com.optic.console.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final EmailSender emailSender;

    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        context.setVariable("content", "email/auth/forgot-password-request :: content");

        String fragmentContent = emailSender.renderFragment(
                "email/auth/forgot-password-request",
                context
        );

        emailSender.sendEmail(
                to,
                "Password Reset - Optic Console",
                fragmentContent
        );
    }
}
