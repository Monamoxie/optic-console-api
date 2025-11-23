package com.optic.console.domain.user.exception;

import com.optic.console.infrastructure.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register a user with an email that already exists.
 */
public class UserAlreadyExistsException extends ApiException {
    
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT);
    }
}
