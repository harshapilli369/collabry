package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.profile.SocialLinkRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserVerificationSocialIT {

    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void influencer_verificationRequest_thenStatus_thenDuplicateRejected() throws Exception {
        String token = login(SEED_INFLUENCER_EMAIL);

        mockMvc.perform(post("/api/users/me/verification/request")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/users/me/verification/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/users/me/verification/request")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You already have a pending verification request"));
    }

    @Test
    void influencer_linkSocial_updatesProfileHandle() throws Exception {
        String token = login(SEED_INFLUENCER_EMAIL);

        SocialLinkRequest link = new SocialLinkRequest();
        link.setPlatform("INSTAGRAM");
        link.setHandle("@it-social-" + System.nanoTime());

        mockMvc.perform(put("/api/users/me/link-social")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(link)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/influencers/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instagramHandle").value(link.getHandle()));
    }

    @Test
    void linkSocial_validationFails_returns400() throws Exception {
        String token = login(SEED_INFLUENCER_EMAIL);

        mockMvc.perform(put("/api/users/me/link-social")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private String login(String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, SEED_DEFAULT_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }
}
