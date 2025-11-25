package com.optic.console.application.service;

import com.optic.console.domain.user.Role;
import com.optic.console.domain.user.User;
import com.optic.console.domain.user.UserStatus;
import com.optic.console.domain.user.UserRepository;
import com.optic.console.domain.user.dto.AuthResponse;
import com.optic.console.domain.user.dto.LoginRequest;
import com.optic.console.domain.user.dto.RegisterRequest;
import com.optic.console.domain.user.exception.UserAlreadyExistsException;
import com.optic.console.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email is already registered");
        }

        try {
            var user = User.builder()
                    .email(request.getEmail().toLowerCase())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(user);
            log.info("User registered successfully with email: {}", request.getEmail());
            
            // TODO: Send verification email
            
        } catch (DataIntegrityViolationException e) {
            log.error("Database error during registration for email: {}", request.getEmail(), e);
            throw new RuntimeException("Registration failed due to a database error", e);
        }
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Failed login attempt - user not found: {}", request.getEmail());
                    return new BadCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt - invalid password for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid credentials");
        }

        var token = jwtService.generateToken(user.getEmail());
        log.info("User logged in successfully: {}", request.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
