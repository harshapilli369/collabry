package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CampaignFlowIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void brand_createsCampaign_thenInvitesInfluencer() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);
        long influencerUserId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        String campaignName = "IT Campaign " + UUID.randomUUID();
        CampaignRequest createReq = new CampaignRequest();
        createReq.setName(campaignName);
        createReq.setBudgetRange(BudgetRange.ONE_K_5K);
        createReq.setDescription("Integration test campaign");

        MvcResult created = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(campaignName))
                .andReturn();

        long campaignId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        MvcResult list = mockMvc.perform(get("/api/campaigns/me")
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode campaigns = objectMapper.readTree(list.getResponse().getContentAsString());
        assertThat(campaigns.isArray()).isTrue();
        boolean found = false;
        for (JsonNode c : campaigns) {
            if (campaignName.equals(c.get("name").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("GET /me should include created campaign").isTrue();

        InvitationRequest inv = new InvitationRequest();
        inv.setInfluencerId(influencerUserId);
        inv.setMessage("Integration test invitation");

        mockMvc.perform(post("/api/campaigns/{id}/invitations", campaignId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inv)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.campaignId").value(campaignId))
                .andExpect(jsonPath("$.influencerId").value(influencerUserId));
    }

    private String loginAccessToken(String email, String password) throws Exception {
        LoginRequest body = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }
}
