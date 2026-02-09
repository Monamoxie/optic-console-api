package com.optic.console.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "New password confirmation is required")
    private String newPasswordConfirmation;
}
