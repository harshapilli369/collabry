package com.group4.backend.service;

import com.group4.backend.dto.AuthResponse;
import com.group4.backend.dto.LoginRequest;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), request.isRememberMe());

        return new AuthResponse(jwtToken, user.getEmail(), user.getRole());
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
            // Create new Google user
            var newUser = new com.group4.backend.model.User(
                    email,
                    "GOOGLE_AUTH_PLACEHOLDER", // Dummy password
                    com.group4.backend.model.Role.USER);
            return userRepository.save(newUser);
        });

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), false);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole());
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
