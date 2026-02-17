package com.optic.console.domain.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Objects;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,100}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase letter and one special character"
    )
    private String newPassword;

    @NotBlank(message = "New password confirmation is required")
    private String newPasswordConfirmation;

    @AssertTrue(message = "New password and confirmation must match")
    public boolean isPasswordConfirmationMatch() {
        return Objects.equals(newPassword, newPasswordConfirmation);
    }
}
