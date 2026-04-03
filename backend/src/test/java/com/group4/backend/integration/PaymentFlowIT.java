package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.payment.PaymentRequest;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentFlowIT {

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
    void brand_createsPayment_listsByCampaign_updatesStatus_influencerSeesPayment() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        long influencerUserId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        String campaignName = "Pay IT " + UUID.randomUUID();
        CampaignRequest createCampaign = new CampaignRequest();
        createCampaign.setName(campaignName);
        createCampaign.setBudgetRange(BudgetRange.FIVE_K_10K);

        MvcResult campRes = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCampaign)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(campRes.getResponse().getContentAsString()).get("id").asLong();

        PaymentRequest payReq = new PaymentRequest();
        payReq.setCampaignId(campaignId);
        payReq.setInfluencerId(influencerUserId);
        payReq.setMilestoneName("First milestone");
        payReq.setAmount(new BigDecimal("1500.00"));
        payReq.setNotes("Integration test payment");

        MvcResult payRes = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.invoiceNumber").exists())
                .andReturn();
        long paymentId = objectMapper.readTree(payRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/payments/campaign/{id}", campaignId)
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(paymentId));

        mockMvc.perform(put("/api/payments/{id}/status", paymentId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PAID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        MvcResult infList = mockMvc.perform(get("/api/payments/me")
                        .header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = objectMapper.readTree(infList.getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode row : arr) {
            if (row.get("id").asLong() == paymentId && "PAID".equals(row.get("status").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("influencer /me should include paid payment").isTrue();

        mockMvc.perform(get("/api/payments/{id}/invoice", paymentId)
                        .header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId));
    }

    @Test
    void influencer_forbiddenOnCreatePayment() throws Exception {
        String influencerToken = loginAccessToken(SEED_INFLUENCER_EMAIL, SEED_DEFAULT_PASSWORD);
        PaymentRequest payReq = new PaymentRequest();
        payReq.setCampaignId(1L);
        payReq.setInfluencerId(1L);
        payReq.setMilestoneName("x");
        payReq.setAmount(BigDecimal.ONE);

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
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
