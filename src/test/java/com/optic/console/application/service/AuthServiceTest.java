package com.optic.console.application.service;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.optic.console.BaseTest;
import com.optic.console.config.ApplicationProperties;
import com.optic.console.domain.auth.TokenType;
import com.optic.console.domain.auth.VerificationToken;
import com.optic.console.domain.user.User;
import com.optic.console.domain.user.UserRepository;
import com.optic.console.domain.user.dto.ForgotPasswordRequest;
import com.optic.console.domain.user.dto.LoginRequest;
import com.optic.console.domain.user.dto.RegisterRequest;
import com.optic.console.domain.user.exception.UserAlreadyExistsException;
import com.optic.console.infrastructure.email.EmailService;
import com.optic.console.infrastructure.security.service.JwtService;

class AuthServiceTest extends BaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private AuthService authService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "SecurePass123!";
    private final String encodedPassword = "$2a$10$testencodedpassword";

    @BeforeEach
    void setUp() {
        // No default stubbings to avoid unnecessary stubbing exceptions
    }

    @Test
    void register_NewUser_ShouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(testPassword);
    }

    @Test
    void register_DuplicateEmail_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        final String testToken = "test.jwt.token";
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(encodedPassword);
        user.setFirstName("Test");
        user.setLastName("User");

        when(userRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(testEmail, false)).thenReturn(testToken);

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(testEmail, response.getEmail());
    }

    @Test
    void login_InvalidPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("wrongPassword");

        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(encodedPassword);

        when(userRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_UserNotFound_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword(testPassword);

        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void register_EmailIsLowercased() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("TEST@EXAMPLE.COM");
        request.setPassword(testPassword);

        when(userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM")).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        authService.register(request);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("test@example.com", userCaptor.getValue().getEmail(), "Email should be lowercased");
    }

    @Test
    void register_DataIntegrityViolation_ShouldThrowRuntimeException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class)))
            .thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate key"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(exception.getMessage().contains("Registration failed"), 
                "Exception message should indicate registration failure");
    }

    @Test
    void handleForgotPasswordRequest_UserExists_SendsEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(testEmail);

        User user = new User();
        user.setEmail(testEmail);
        user.setFirstName("Test");
        user.setLastName("User");

        String testToken = "hello_world_token_123";
        String baseUrl = "http://localhost:8080";
        String expectedResetLink = baseUrl + "/reset-password?token=" + testToken;

        when(userRepository.findByEmailIgnoreCase(testEmail)).thenReturn(Optional.of(user));
        when(applicationProperties.getUrl()).thenReturn(baseUrl);
        
        VerificationToken token = new VerificationToken();
        token.setToken(testToken);

        when(verificationTokenService.createToken(eq(user), eq(TokenType.PASSWORD_RESET), eq(Duration.ofHours(1))))
                .thenReturn(token);

        assertDoesNotThrow(() -> authService.handleForgotPasswordRequest(request));

        verify(verificationTokenService).createToken(eq(user), eq(TokenType.PASSWORD_RESET), eq(Duration.ofHours(1)));
        verify(emailService).sendPasswordResetEmail(
                eq(testEmail),
                eq("Test User"),
                eq(expectedResetLink)
        );
    }

    @Test
    void handleForgotPasswordRequest_UserDoesNotExist_DoesNotThrow() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.handleForgotPasswordRequest(request));
    }
}