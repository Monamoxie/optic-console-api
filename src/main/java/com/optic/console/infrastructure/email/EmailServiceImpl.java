package com.optic.console.infrastructure.email;

import com.optic.console.application.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final EmailSender emailSender;

    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);

        emailSender.sendEmail(
                to,
                "Password Reset - Optic Console",
                "email/auth/forgot-password-request",
                context
        );
    }
}
