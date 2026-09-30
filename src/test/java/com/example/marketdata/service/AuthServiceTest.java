package com.example.marketdata.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void shouldAuthenticateValidUser() {
        assertTrue(authService.authenticate("admin", "admin"));
    }

    @Test
    void shouldRejectInvalidUser() {
        assertFalse(authService.authenticate("admin", "wrong"));
    }
}
