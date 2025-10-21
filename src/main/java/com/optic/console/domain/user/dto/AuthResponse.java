package com.optic.console.domain.user.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
}