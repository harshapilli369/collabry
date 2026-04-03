package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.service.ai.GroqApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises Groq-backed controller paths with a stubbed {@link GroqApiClient} (no outbound HTTP to Groq).
 */
@SpringBootTest
@AutoConfigureMockMvc
class GroqAiMockedIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @MockBean
    private GroqApiClient groqApiClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void stubGroq() {
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("Polished copy from stubbed Groq client.");
    }

    @Test
    void generateDescription_returnsDescriptionFromGroqClient() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/campaigns/generate-description")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Mocked IT Campaign",
                                "goal", "Reach",
                                "budget", "5k"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Polished copy from stubbed Groq client."));
    }

    @Test
    void enhanceBio_returnsEnhancedBioFromGroqClient() throws Exception {
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/influencers/enhance-bio")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Raw bio text."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enhancedBio").value("Polished copy from stubbed Groq client."));
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
