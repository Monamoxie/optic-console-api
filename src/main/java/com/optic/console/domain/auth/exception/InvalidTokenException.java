package com.optic.console.domain.auth.exception;

import com.optic.console.infrastructure.exception.ApiException;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register a user with an email that already exists.
 */
public class InvalidTokenException extends ApiException {

    private static final String DEFAULT_MESSAGE = "The token is invalid";

    public InvalidTokenException(@Nullable String message) {
        super(message != null ? message : DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
