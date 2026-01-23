package com.optic.console.infrastructure.exception;

import com.optic.console.domain.user.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errors.put(field.substring(field.lastIndexOf('.') + 1), violation.getMessage());
        });
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Invalid email or password")
                        .build());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiExceptions(ApiException apiException) {
        return ResponseEntity.status(apiException.getStatus())
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(apiException.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("An unexpected error occurred:" + cleanErrorMessage(ex.getMessage()))
                        .build());
    }

    private String cleanErrorMessage(String message) {

        if (message == null) {
            return "An unexpected error occurred";
        }

        boolean isProduction = environment.acceptsProfiles(Profiles.of("prod"));

        if (!isProduction) {
           return message;
        }

        // Remove Java method signatures
        if (message.contains("public ")) {
            message = message.substring(0, message.indexOf("public ")).trim();
        }

        // Remove any class names in the format com.package.ClassName
        message = message.replaceAll("\\b[a-z]+(?:\\.[a-z0-9]+)*\\.[A-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z0-9_]+)*\\b", "");

        // Clean up any double spaces or leading/trailing punctuation
        message = message.replaceAll("\\s+", " ").trim();
        message = message.replaceAll("^[\\s,;:.!?]+", "").trim();

        return message.isEmpty() ? "An unexpected error occurred" : message;
    }

    @ExceptionHandler(DebugException.class)
    public ResponseEntity<String> handleDebugException(DebugException ex) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

            String formatted = objectMapper.writeValueAsString(ex.getDebugItems());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(formatted);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to format debug output\"}");
        }
    }
}
