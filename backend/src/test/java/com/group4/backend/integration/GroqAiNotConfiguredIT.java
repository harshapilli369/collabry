package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Default test profile uses {@code spring.ai.groq.api-key=dummy}; controllers must reject AI endpoints without calling Groq.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GroqAiNotConfiguredIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateDescription_whenGroqNotConfigured_returns400() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/campaigns/generate-description")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "IT Campaign",
                                "goal", "Awareness",
                                "budget", "ONE_K_5K"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("AI service is not configured"));
    }

    @Test
    void enhanceBio_whenGroqNotConfigured_returns400() throws Exception {
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/influencers/enhance-bio")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Short bio for IT."))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("AI service is not configured"));
    }

    private String loginAccessToken(String email, String password) throws Exception {
        LoginRequest body = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
