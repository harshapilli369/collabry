package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.model.Role;
import com.group4.backend.repository.user.PendingSignupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack HTTP tests: security filter chain, validation, JPA, and registration flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PendingSignupRepository pendingSignupRepository;

    @Test
    void register_validRequest_persistsPendingSignup() throws Exception {
        String email = "it-" + UUID.randomUUID() + "@example.com";
        SignupRequest request = new SignupRequest(email, "Password1", Role.BRAND);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());

        assertThat(pendingSignupRepository.existsByEmail(email)).isTrue();
    }

    @Test
    void register_invalidPassword_returns400() throws Exception {
        SignupRequest request = new SignupRequest("badpw@test.com", "short", Role.BRAND);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_emailAlreadyRegistered_returns409() throws Exception {
        SignupRequest request = new SignupRequest("brand@collabry.com", "Password1", Role.INFLUENCER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }
}
