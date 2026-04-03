package com.group4.backend.controller.auth;

import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.auth.ResetPasswordRequest;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.dto.auth.SignupResponse;
import com.group4.backend.dto.auth.TokenRequest;
import com.group4.backend.exception.DuplicateEmailException;
import com.group4.backend.service.auth.AuthService;
import com.group4.backend.service.auth.PasswordResetService;
import com.group4.backend.service.auth.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(RegistrationService registrationService, AuthService authService,
                           PasswordResetService passwordResetService) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<SignupResponse> register(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<AuthResponse> confirmEmail(@RequestParam String token) {
        AuthResponse response = registrationService.confirmEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody TokenRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request.getToken(), request.getRole()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        System.out.println("Processing forgot password for: " + request.get("email"));
        try {
            passwordResetService.forgotPassword(request.get("email"));
            return ResponseEntity.ok(Map.of("message", "Reset link sent"));
        } catch (Exception e) {
            System.err.println("Error in forgot password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e instanceof DuplicateEmailException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }
}
