package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.DeliverableUpdateRequest;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CollaborationDeliverableIT {

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
    void acceptInvitation_submitDeliverable_brandApproves_andAppearsInCollaborationHistory() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        long influencerUserId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        String campaignName = "Collab IT " + UUID.randomUUID();
        CampaignRequest createReq = new CampaignRequest();
        createReq.setName(campaignName);
        createReq.setBudgetRange(BudgetRange.ONE_K_5K);

        MvcResult campaignRes = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(campaignRes.getResponse().getContentAsString()).get("id").asLong();

        InvitationRequest invReq = new InvitationRequest();
        invReq.setInfluencerId(influencerUserId);
        invReq.setMessage("Collab deliverable flow");

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        DeliverableUpdateRequest deliverable = new DeliverableUpdateRequest();
        deliverable.setDeliverableStatus("SUBMITTED");
        deliverable.setContentLink("https://example.com/it-deliverable");
        mockMvc.perform(put("/api/collaborations/{id}/deliverable", invitationId)
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliverable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliverableStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.contentLink").value("https://example.com/it-deliverable"));

        mockMvc.perform(put("/api/collaborations/{id}/approve", invitationId)
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliverableStatus").value("APPROVED"));

        MvcResult history = mockMvc.perform(get("/api/collaborations/me")
                        .header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(history.getResponse().getContentAsString());
        assertThat(arr.isArray()).isTrue();
        boolean found = false;
        for (JsonNode row : arr) {
            if (row.get("id").asLong() == invitationId
                    && "APPROVED".equals(row.get("deliverableStatus").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("collaboration history should list approved deliverable").isTrue();
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
