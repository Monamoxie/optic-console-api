package com.optic.console.domain.user.exception;

import com.optic.console.infrastructure.exception.ApiException;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public class UserDoesNotExistException extends ApiException {

    private static final String DEFAULT_MESSAGE = "User does not exist";

    public UserDoesNotExistException() {
        super(DEFAULT_MESSAGE, HttpStatus.NOT_FOUND);
    }

    public UserDoesNotExistException(@Nullable String message) {
        super(message != null ? message : DEFAULT_MESSAGE, HttpStatus.NOT_FOUND);
    }

    public UserDoesNotExistException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
