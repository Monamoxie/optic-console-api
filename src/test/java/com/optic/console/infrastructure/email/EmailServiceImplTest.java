package com.optic.console.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private final String testEmail = "test@example.com";
    private final String testName = "Test User";
    private final String testResetLink = "https://example.com/reset-password?token=abc123";

    @Test
    void sendPasswordResetEmail_Success() {

        String expectedTemplate = "email/auth/forgot-password-request";
        String expectedSubject = "Password Reset - Optic Console";


        emailService.sendPasswordResetEmail(testEmail, testName, testResetLink);


        verify(emailSender).renderFragment(eq(expectedTemplate), contextCaptor.capture());
        
        // Verify context variables
        Context capturedContext = contextCaptor.getValue();
        assertEquals(testName, capturedContext.getVariable("name"));
        assertEquals(testResetLink, capturedContext.getVariable("resetLink"));
        

        verify(emailSender).sendEmail(
                eq(testEmail),
                eq(expectedSubject),
                anyString()
        );
    }

    @Test
    void sendPasswordResetEmail_EmptyName_StillSendsEmail() {

        emailService.sendPasswordResetEmail(testEmail, "", testResetLink);


        verify(emailSender).renderFragment(anyString(), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals("", capturedContext.getVariable("name"));
    }

    @Test
    void sendPasswordResetEmail_EmailSendingFails_LogsError() {

        String errorMessage = "Failed to send email";
        doThrow(new RuntimeException(errorMessage))
            .when(emailSender).sendEmail(anyString(), anyString(), anyString());


        assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordResetEmail(testEmail, testName, testResetLink);
        });
        

        verify(emailSender).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendPasswordResetEmail_NullParameters_ThrowsException() {

        assertThrows(NullPointerException.class, () -> {
            emailService.sendPasswordResetEmail(null, testName, testResetLink);
        });


        assertThrows(NullPointerException.class, () -> {
            emailService.sendPasswordResetEmail(testEmail, testName, null);
        });
        

        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail(testEmail, null, testResetLink);
        });
    }
}
