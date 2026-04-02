package com.group4.backend.service.auth;

import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String TEST_BRAND_EMAIL = "brand@collabry";
    private static final String EMAIL_JSON_PREFIX = "\"email\": \"";
    private static final int EMAIL_JSON_PREFIX_LENGTH = EMAIL_JSON_PREFIX.length();

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtUtils jwtUtils,
                        AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), request.isRememberMe());
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getId(),
                isEffectivelyVerified(user));
    }

    public AuthResponse loginWithGoogle(String accessToken, String roleStr) {
        String email = fetchEmailFromGoogle(accessToken);
        if (email == null) {
            throw new RuntimeException("Invalid Google Token");
        }

        Role role = parseRole(roleStr);
        final Role assignedRole = role;
        var user = userRepository.findByEmail(email).orElseGet(() -> {
            var newUser = new User(email, "GOOGLE_AUTH_PLACEHOLDER", assignedRole);
            return userRepository.save(newUser);
        });

        if (!user.isActive()) {
            throw new DisabledException("Account deactivated");
        }

        var jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), false);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getId(),
                isEffectivelyVerified(user));
    }

    private Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) {
            return Role.INFLUENCER;
        }
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            return role == Role.USER ? Role.INFLUENCER : role;
        } catch (IllegalArgumentException e) {
            return Role.INFLUENCER;
        }
    }

    private static boolean isEffectivelyVerified(User user) {
        return user.isVerified() || TEST_BRAND_EMAIL.equalsIgnoreCase(user.getEmail());
    }

    private String fetchEmailFromGoogle(String accessToken) {
        try {
            java.net.URL url = new java.net.URL("https://www.googleapis.com/oauth2/v3/userinfo");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()))) {
                    String response = br.lines().collect(java.util.stream.Collectors.joining());
                    if (response.contains("\"email\": \"")) {
                        int start = response.indexOf(EMAIL_JSON_PREFIX) + EMAIL_JSON_PREFIX_LENGTH;
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
