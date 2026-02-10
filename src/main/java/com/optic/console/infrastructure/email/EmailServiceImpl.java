package com.optic.console.infrastructure.email;

import com.optic.console.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final EmailSender emailSender;
    private final ApplicationProperties applicationProperties;

    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        if (to == null || resetLink == null) {
            throw new NullPointerException("Email and reset link cannot be null");
        }

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

    @Override
    public void sendEmailVerificationEmail(String to, String verificationLink) {
        if (to == null || verificationLink == null) {
            throw new NullPointerException("Recipient email and verification link cannot be null");
        }

        String templateName = "email/auth/email-verification";
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        context.setVariable("content", templateName + " :: content");

        String fragmentContent = emailSender.renderFragment(templateName, context);
        emailSender.sendEmail(to,
                "Verify your Email - " + applicationProperties.getName(),
                fragmentContent);
    }
}
