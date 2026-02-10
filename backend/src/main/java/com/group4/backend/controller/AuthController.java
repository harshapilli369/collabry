package com.group4.backend.controller;

import com.group4.backend.dto.AuthResponse;
import com.group4.backend.dto.LoginRequest;
import com.group4.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody com.group4.backend.dto.TokenRequest request) {
        return ResponseEntity.ok(service.loginWithGoogle(request.getToken()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        System.out.println("Processing forgot password for: " + request.get("email"));
        try {
            service.forgotPassword(request.get("email"));
            return ResponseEntity.ok(java.util.Map.of("message", "Reset link sent"));
        } catch (Exception e) {
            System.err.println("Error in forgot password: " + e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> request) {
        service.resetPassword(request.get("token"), request.get("newPassword"));
        return ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully"));
    }
}
