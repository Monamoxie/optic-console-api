package com.optic.console.application.service;

import com.optic.console.BaseTest;
import com.optic.console.domain.user.User;
import com.optic.console.domain.user.UserRepository;
import com.optic.console.domain.user.dto.LoginRequest;
import com.optic.console.domain.user.dto.RegisterRequest;
import com.optic.console.domain.user.exception.UserAlreadyExistsException;
import com.optic.console.infrastructure.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest extends BaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

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
        request.setFirstName("Test");
        request.setLastName("User");

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
        request.setFirstName("Test");
        request.setLastName("User");

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
        when(jwtService.generateToken(testEmail)).thenReturn(testToken);

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
}