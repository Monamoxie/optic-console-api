package com.optic.console.application.service;

import com.optic.console.domain.user.*;
import com.optic.console.domain.user.dto.AuthResponse;
import com.optic.console.domain.user.dto.LoginRequest;
import com.optic.console.domain.user.dto.RegisterRequest;
import com.optic.console.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .status("active")
                .build();
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        var token = jwtService.generateToken(user.getEmail());
        var response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        return response;
    }
}
