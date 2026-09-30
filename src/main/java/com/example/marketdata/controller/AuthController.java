package com.example.marketdata.controller;

import com.example.marketdata.dto.LoginRequest;
import com.example.marketdata.dto.LoginResponse;
import com.example.marketdata.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        boolean success = authService.authenticate(request.username(), request.password());

        return success
                ? new LoginResponse(true, "Login successful")
                : new LoginResponse(false, "Invalid username or password");
    }
}
