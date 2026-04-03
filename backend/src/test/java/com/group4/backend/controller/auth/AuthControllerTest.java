package com.group4.backend.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.auth.ResetPasswordRequest;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.dto.auth.SignupResponse;
import com.group4.backend.dto.auth.TokenRequest;
import com.group4.backend.exception.DuplicateEmailException;
import com.group4.backend.model.Role;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.auth.AuthService;
import com.group4.backend.service.auth.PasswordResetService;
import com.group4.backend.service.auth.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RegistrationService registrationService;
    @MockBean
    private AuthService authService;
    @MockBean
    private PasswordResetService passwordResetService;
    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void register_validRequest_returns201() throws Exception {
        SignupRequest request = new SignupRequest("user@test.com", "Password1", Role.BRAND);
        SignupResponse response = new SignupResponse("Check your email to confirm registration.");
        when(registrationService.register(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        SignupRequest request = new SignupRequest("existing@test.com", "Password1", Role.INFLUENCER);
        when(registrationService.register(any(SignupRequest.class)))
                .thenThrow(new DuplicateEmailException("An account with this email already exists."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    void register_validationFails_returns400() throws Exception {
        Map<String, String> badRequest = Map.of(
                "email", "not-an-email",
                "password", "short",
                "role", "BRAND"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(";")));
    }

    @Test
    void confirmEmail_validToken_returns200() throws Exception {
        AuthResponse response = new AuthResponse("jwt", "user@test.com", Role.BRAND, 1L, true);
        when(registrationService.confirmEmail("valid-token")).thenReturn(response);

        mockMvc.perform(get("/api/auth/confirm-email").param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("BRAND"));
    }

    @Test
    void confirmEmail_invalidToken_returns400() throws Exception {
        when(registrationService.confirmEmail("bad-token"))
                .thenThrow(new RuntimeException("Invalid or expired confirmation link."));

        mockMvc.perform(get("/api/auth/confirm-email").param("token", "bad-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired confirmation link."));
    }

    @Test
    void login_validRequest_returns200() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password1");
        AuthResponse response = new AuthResponse("jwt", "user@test.com", Role.INFLUENCER, 2L, true);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void googleLogin_validToken_returns200() throws Exception {
        TokenRequest request = new TokenRequest();
        request.setToken("google-id-token");
        AuthResponse response = new AuthResponse("jwt", "user@gmail.com", Role.BRAND, 1L, true);
        when(authService.loginWithGoogle("google-id-token", null)).thenReturn(response);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"));
    }

    @Test
    void forgotPassword_success_returns200() throws Exception {
        doNothing().when(passwordResetService).forgotPassword("user@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset link sent"));
    }

    @Test
    void forgotPassword_serviceThrows_returns500() throws Exception {
        doThrow(new RuntimeException("Email not found")).when(passwordResetService).forgotPassword(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@test.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Email not found"));
    }

    @Test
    void resetPassword_validRequest_returns200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("reset-token");
        request.setNewPassword("NewPassword1");
        doNothing().when(passwordResetService).resetPassword("reset-token", "NewPassword1");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }
}
