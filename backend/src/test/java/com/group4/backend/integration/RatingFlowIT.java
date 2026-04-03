package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.rating.RatingRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RatingFlowIT {

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
    void brand_submitsRating_afterAcceptedInvitation() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        long influencerUserId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        String campaignName = "Rate IT " + UUID.randomUUID();
        CampaignRequest createCampaign = new CampaignRequest();
        createCampaign.setName(campaignName);
        createCampaign.setBudgetRange(BudgetRange.ONE_K_5K);

        MvcResult campRes = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCampaign)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(campRes.getResponse().getContentAsString()).get("id").asLong();

        InvitationRequest invReq = new InvitationRequest();
        invReq.setInfluencerId(influencerUserId);
        invReq.setMessage("For rating IT");

        MvcResult invRes = mockMvc.perform(post("/api/campaigns/{id}/invitations", campaignId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long invitationId = objectMapper.readTree(invRes.getResponse().getContentAsString()).get("id").asLong();

        RespondRequest accept = new RespondRequest();
        accept.setAction("ACCEPT");
        mockMvc.perform(post("/api/invitations/{id}/respond", invitationId)
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accept)))
                .andExpect(status().isOk());

        RatingRequest rating = new RatingRequest();
        rating.setInvitationId(invitationId);
        rating.setRating(5);
        rating.setReview("Great collaboration (integration test).");

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationId").value(invitationId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.review").value("Great collaboration (integration test)."));
    }

    @Test
    void influencer_forbiddenOnSubmitRating() throws Exception {
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        RatingRequest rating = new RatingRequest();
        rating.setInvitationId(1L);
        rating.setRating(4);

        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rating)))
                .andExpect(status().isForbidden());
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
