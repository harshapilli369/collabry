package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.NegotiationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.invitation.UpdateInvitationRequest;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvitationLifecycleIT {

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
    void influencer_rejectsInvitation() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        String influencerToken = login(SEED_INFLUENCER_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();
        long invId = createCampaignAndInvitation(brandToken, influencerId);

        RespondRequest reject = new RespondRequest();
        reject.setAction("REJECT");
        mockMvc.perform(post("/api/invitations/{id}/respond", invId)
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void negotiate_thenBrandConfirmsTerms_becomesConfirmed() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        String influencerToken = login(SEED_INFLUENCER_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();
        long invId = createCampaignAndInvitation(brandToken, influencerId);

        NegotiationRequest neg = new NegotiationRequest();
        neg.setProposedAmount(new BigDecimal("2500"));
        neg.setProposedTimeline("30 days");
        neg.setProposedDeliverables("2 reels");
        mockMvc.perform(put("/api/invitations/{id}/negotiate", invId)
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(neg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEGOTIATING"));

        mockMvc.perform(post("/api/invitations/{id}/confirm-terms", invId)
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void brand_withdrawsPendingInvitation() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();
        long invId = createCampaignAndInvitation(brandToken, influencerId);

        mockMvc.perform(delete("/api/invitations/{id}", invId)
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/invitations/sent").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk());
    }

    @Test
    void brand_updatesPendingInvitation() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();
        long invId = createCampaignAndInvitation(brandToken, influencerId);

        UpdateInvitationRequest upd = new UpdateInvitationRequest();
        upd.setMessage("Updated brand message for IT");
        upd.setProposedAmount(new BigDecimal("1800"));
        mockMvc.perform(put("/api/invitations/{id}", invId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandMessage").value("Updated brand message for IT"));
    }

    @Test
    void influencer_getInvitationDetail_byId() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        String influencerToken = login(SEED_INFLUENCER_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();
        long invId = createCampaignAndInvitation(brandToken, influencerId);

        mockMvc.perform(get("/api/invitations/{id}", invId)
                        .header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invId))
                .andExpect(jsonPath("$.campaignName").exists());
    }

    @Test
    void brand_forbiddenOnInfluencerInvitationEndpoints() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        mockMvc.perform(get("/api/invitations/me").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isForbidden());
    }

    private String login(String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, SEED_DEFAULT_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private long createCampaignAndInvitation(String brandToken, long influencerUserId) throws Exception {
        CampaignRequest c = new CampaignRequest();
        c.setName("Inv IT " + UUID.randomUUID());
        c.setBudgetRange(BudgetRange.ONE_K_5K);
        MvcResult camp = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        InvitationRequest inv = new InvitationRequest();
        inv.setInfluencerId(influencerUserId);
        inv.setMessage("Lifecycle IT");
        MvcResult res = mockMvc.perform(post("/api/campaigns/{id}/invitations", campaignId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inv)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }
}
