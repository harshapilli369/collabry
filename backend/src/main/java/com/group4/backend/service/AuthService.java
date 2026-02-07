package com.group4.backend.service;

import com.group4.backend.dto.AuthResponse;
import com.group4.backend.dto.LoginRequest;
import com.group4.backend.dto.SignupRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(SignupRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin registration is not allowed");
        }
        if (request.getRole() == Role.INFLUENCER && (request.getDisplayName() == null || request.getDisplayName().isBlank())) {
            throw new IllegalArgumentException("Display name is required for influencers");
        }
        if (request.getRole() == Role.BRAND && (request.getCompanyName() == null || request.getCompanyName().isBlank())) {
            throw new IllegalArgumentException("Company name is required for brands");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getRole(),
                request.getDisplayName(),
                request.getCompanyName()
        );
        user = userRepository.save(user);

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getDisplayName(), user.getCompanyName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getDisplayName(), user.getCompanyName());
    }

    // New Google Login Method
    public AuthResponse loginWithGoogle(String accessToken) {
        // In a real production app, verify the token via Google's API to get the email
        // For simplicity/demo with implicit flow/access token:
        // HttpRequest to https://www.googleapis.com/oauth2/v3/userinfo?access_token=...

        // Mocking the extraction for now or assume token IS the email for extremely
        // simple test?
        // No, let's do a basic fetch implementation if possible, or just accept the
        // token
        // IF we trust the client (INSECURE) - but good for a "quick fix" demo without
        // messing with HttpClient dependencies heavily.

        // Let's assume we implement a real verify or fetch user info.
        // To make it actually work for the assignment:
        // We will fetch the user info using java.net.HttpURLConnection (no external dep
        // needed)

        String email = fetchEmailFromGoogle(accessToken);

        if (email == null) {
            throw new RuntimeException("Invalid Google Token");
        }

        // Check if user exists
        var user = userRepository.findByEmail(email).orElseGet(() -> {
            // Create new Google user (default to INFLUENCER)
            var newUser = new User(email, "GOOGLE_AUTH_PLACEHOLDER", Role.INFLUENCER, null, null);
            return userRepository.save(newUser);
        });

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getDisplayName(), user.getCompanyName());
    }

    private String fetchEmailFromGoogle(String accessToken) {
        try {
            java.net.URL url = new java.net.URL("https://www.googleapis.com/oauth2/v3/userinfo");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()))) {
                    String response = br.lines().collect(java.util.stream.Collectors.joining());
                    // Simple string parsing to avoid Jackson complexity if not needed
                    // Response is JSON: { "sub": "...", "email": "UserEmail...", ... }
                    if (response.contains("\"email\": \"")) {
                        int start = response.indexOf("\"email\": \"") + 10;
                        int end = response.indexOf("\"", start);
                        return response.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
