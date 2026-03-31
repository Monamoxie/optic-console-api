package com.optic.console.api.auth;

import com.optic.console.application.service.AuthService;
import com.optic.console.domain.auth.dto.EmailVerificationRequest;
import com.optic.console.domain.user.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .success(true)
                        .message("User registered successfully. Please check your email for verification.")
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .data(authResponse)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.handleForgotPasswordRequest(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset email sent successfully.")
                        .build()
        );
    }

    @GetMapping("/verification/reset-password")
    public ResponseEntity<ApiResponse<?>> verifyResetPasswordRequest(
            @RequestParam("token") @NotBlank String token
    ) {
            authService.handlePasswordResetTokenVerification(token);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Password reset token is valid")
                            .build()
            );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.handlePasswordReset(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Password reset ws successful")
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rawToken = authorizationHeader.substring(7);

        try {
            AuthResponse authResponse = authService.getCurrentUser(rawToken);
            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/verification/email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        authService.handleEmailVerification(request.getToken());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Email verification was successful")
                        .build()
        );
    }

}