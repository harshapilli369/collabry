package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.model.PendingSignup;
import com.group4.backend.model.Role;
import com.group4.backend.repository.user.PendingSignupRepository;
import com.group4.backend.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthEmailConfirmIT {

    private static final String PASSWORD = "Password1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PendingSignupRepository pendingSignupRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void register_thenConfirm_createsUserAndReturnsJwt() throws Exception {
        String email = "confirm-it-" + UUID.randomUUID() + "@example.com";
        SignupRequest signup = new SignupRequest(email, PASSWORD, Role.BRAND);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        String token = confirmationTokenForEmail(email);
        assertThat(token).isNotBlank();

        MvcResult confirm = mockMvc.perform(get("/api/auth/confirm-email").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        JsonNode body = objectMapper.readTree(confirm.getResponse().getContentAsString());
        assertThat(body.get("token").asText()).isNotBlank();

        assertThat(userRepository.existsByEmail(email)).isTrue();
        assertThat(pendingSignupRepository.existsByEmail(email)).isFalse();

        LoginRequest login = new LoginRequest(email, PASSWORD);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void confirmEmail_invalidToken_returns400() throws Exception {
        mockMvc.perform(get("/api/auth/confirm-email").param("token", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired confirmation link."));
    }

    private String confirmationTokenForEmail(String email) {
        List<PendingSignup> rows = entityManager
                .createQuery("SELECT p FROM PendingSignup p WHERE p.email = :email", PendingSignup.class)
                .setParameter("email", email)
                .getResultList();
        assertThat(rows).hasSize(1);
        return rows.get(0).getToken();
    }
}
