package com.optic.console.infrastructure.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;

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
    private final String forgotPasswordRequestEmailTemplate = "email/auth/forgot-password-request";

    @Test
    void sendPasswordResetEmail_Success() {
        when(emailSender.renderFragment(anyString(), any())).thenReturn("rendered-email");

        emailService.sendPasswordResetEmail(testEmail, testName, testResetLink);

        verify(emailSender).renderFragment(eq(forgotPasswordRequestEmailTemplate), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertEquals(testName, capturedContext.getVariable("name"));
        assertEquals(testResetLink, capturedContext.getVariable("resetLink"));
        assertEquals(forgotPasswordRequestEmailTemplate + " :: content", capturedContext.getVariable("content"));
        
        verify(emailSender).sendEmail(
                eq(testEmail),
                eq("Password Reset - Optic Console"),
                eq("rendered-email")
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
    void sendPasswordResetEmail_EmailSendingFails_PropagatesException() {
        when(emailSender.renderFragment(anyString(), any())).thenReturn("rendered-email");

        String errorMessage = "Failed to send email";
        doThrow(new RuntimeException(errorMessage))
            .when(emailSender).sendEmail(anyString(), anyString(), anyString());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordResetEmail(testEmail, testName, testResetLink);
        });
        
        assertEquals(errorMessage, exception.getMessage());
        verify(emailSender).renderFragment(eq(forgotPasswordRequestEmailTemplate), any());
        verify(emailSender).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendPasswordResetEmail_NullEmail_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            emailService.sendPasswordResetEmail(null, testName, testResetLink);
        });
    }

    @Test
    void sendPasswordResetEmail_NullResetLink_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            emailService.sendPasswordResetEmail(testEmail, testName, null);
        });
    }

    @Test
    void sendPasswordResetEmail_NullName_StillSendsEmail() {
        when(emailSender.renderFragment(anyString(), any())).thenReturn("rendered-email");

        emailService.sendPasswordResetEmail(testEmail, null, testResetLink);

        verify(emailSender).renderFragment(anyString(), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertNull(capturedContext.getVariable("name"), "Name should be null");
        assertEquals(testResetLink, capturedContext.getVariable("resetLink"));
        verify(emailSender).sendEmail(eq(testEmail), eq("Password Reset - Optic Console"), eq("rendered-email"));
    }

    @Test
    void sendPasswordResetEmail_RenderFragmentReturnsNull_HandlesGracefully() {
        when(emailSender.renderFragment(anyString(), any())).thenReturn(null);

        emailService.sendPasswordResetEmail(testEmail, testName, testResetLink);

        verify(emailSender).sendEmail(eq(testEmail), eq("Password Reset - Optic Console"), eq(null));
    }
}
