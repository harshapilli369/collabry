package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.profile.CollaborationAvailabilityRequest;
import com.group4.backend.dto.profile.InfluencerProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InfluencerProfileSearchIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void influencer_updatesProfile_brandSearchFindsByNiche_andAvailabilityToggleWorks() throws Exception {
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);

        String uniqueNiche = "IT-Niche-" + UUID.randomUUID();
        InfluencerProfileRequest update = new InfluencerProfileRequest();
        update.setName("Search Test Creator");
        update.setAge(22);
        update.setLocation("Halifax, NS");
        update.setNiche(uniqueNiche);
        update.setBio("Bio for search integration test.");
        update.setFollowerCount(50_000L);
        update.setEngagementRate(new BigDecimal("5.5"));
        update.setRate(new BigDecimal("500"));
        update.setInstagramHandle("@searchit");
        update.setSaveAsDraft(false);

        mockMvc.perform(put("/api/influencers/me")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.niche").value(uniqueNiche))
                .andExpect(jsonPath("$.openToCollaborations").value(true));

        mockMvc.perform(get("/api/influencers/me")
                        .header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.niche").value(uniqueNiche));

        MvcResult search = mockMvc.perform(get("/api/influencers/search")
                        .header("Authorization", "Bearer " + brandToken)
                        .param("niche", uniqueNiche.substring(0, 12)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode results = objectMapper.readTree(search.getResponse().getContentAsString());
        assertThat(results.isArray()).isTrue();
        boolean found = false;
        for (JsonNode row : results) {
            if (uniqueNiche.equals(row.get("niche").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("brand search should return updated niche").isTrue();

        CollaborationAvailabilityRequest avail = new CollaborationAvailabilityRequest();
        avail.setOpenToCollaborations(false);
        mockMvc.perform(put("/api/influencers/me/collaboration-availability")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(avail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openToCollaborations").value(false));

        MvcResult availableOnly = mockMvc.perform(get("/api/influencers/search")
                        .header("Authorization", "Bearer " + brandToken)
                        .param("niche", uniqueNiche.substring(0, 12))
                        .param("availableOnly", "true"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode filtered = objectMapper.readTree(availableOnly.getResponse().getContentAsString());
        for (JsonNode row : filtered) {
            assertThat(row.get("niche").asText()).isNotEqualTo(uniqueNiche);
        }
    }

    @Test
    void search_invalidFollowerRange_returns400() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/influencers/search")
                        .header("Authorization", "Bearer " + brandToken)
                        .param("minFollowers", "1000")
                        .param("maxFollowers", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minFollowers cannot be greater than maxFollowers"));
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
