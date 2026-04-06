package com.skinmarket.marketplace.controller;

import com.skinmarket.marketplace.dto.AuthRequest;
import com.skinmarket.marketplace.dto.AuthResponse;
import com.skinmarket.marketplace.dto.UserRegisterRequest;
import com.skinmarket.marketplace.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(authService.register(userRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.login(authRequest));
    }
}