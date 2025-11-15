package com.optic.console.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optic.console.DebugUtil;
import com.optic.console.application.service.AuthService;
import com.optic.console.domain.user.dto.AuthResponse;
import com.optic.console.domain.user.dto.LoginRequest;
import com.optic.console.domain.user.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request)  throws Exception {
        log.info("This is an info log :::: HELLO WORLD 2");
        authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}