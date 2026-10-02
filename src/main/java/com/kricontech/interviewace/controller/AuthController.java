package com.kricontech.interviewace.controller;

import com.kricontech.interviewace.dto.AuthResponse;
import com.kricontech.interviewace.dto.LoginRequest;
import com.kricontech.interviewace.dto.RegisterRequest;
import com.kricontech.interviewace.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/google")
    public ResponseEntity<Void> googleAuth() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authService.buildGoogleAuthorizationUrl()))
                .build();
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam String code) {
        String redirectUrl = authService.handleGoogleCallback(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/github")
    public ResponseEntity<Void> githubAuth() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authService.buildGithubAuthorizationUrl()))
                .build();
    }

    @GetMapping("/github/callback")
    public ResponseEntity<Void> githubCallback(@RequestParam String code) {
        String redirectUrl = authService.handleGithubCallback(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
