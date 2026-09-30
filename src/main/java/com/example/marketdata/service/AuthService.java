package com.example.marketdata.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // Intentionally hardcoded because the assignment allows a hardcoded user store.
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public boolean authenticate(String username, String password) {
        return USERNAME.equals(username) && PASSWORD.equals(password);
    }
}
