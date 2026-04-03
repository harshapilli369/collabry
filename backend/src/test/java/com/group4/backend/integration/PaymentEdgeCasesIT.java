package com.group4.backend.integration;

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
import java.time.LocalDate;
import java.util.HashMap;
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
class PaymentEdgeCasesIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_ADMIN_EMAIL = "admin@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void overduePendingPayment_appearsInDelayedList() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        CampaignRequest c = new CampaignRequest();
        c.setName("Delayed IT " + UUID.randomUUID());
        c.setBudgetRange(BudgetRange.UNDER_1K);
        MvcResult camp = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        PaymentRequest pay = new PaymentRequest();
        pay.setCampaignId(campaignId);
        pay.setInfluencerId(influencerId);
        pay.setMilestoneName("Overdue milestone");
        pay.setAmount(new BigDecimal("100.00"));
        pay.setDueDate(LocalDate.now().minusDays(7));

        MvcResult payRes = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        long paymentId = objectMapper.readTree(payRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/payments/delayed").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    assertThat(body).contains("\"id\":" + paymentId);
                    assertThat(body).contains("DELAYED");
                });
    }

    @Test
    void getInvoice_unrelatedUser_returns400() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        CampaignRequest c = new CampaignRequest();
        c.setName("Invoice IT " + UUID.randomUUID());
        c.setBudgetRange(BudgetRange.UNDER_1K);
        MvcResult camp = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        PaymentRequest pay = new PaymentRequest();
        pay.setCampaignId(campaignId);
        pay.setInfluencerId(influencerId);
        pay.setMilestoneName("M1");
        pay.setAmount(BigDecimal.TEN);
        MvcResult payRes = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay)))
                .andExpect(status().isCreated())
                .andReturn();
        long paymentId = objectMapper.readTree(payRes.getResponse().getContentAsString()).get("id").asLong();

        String adminToken = login(SEED_ADMIN_EMAIL);
        mockMvc.perform(get("/api/payments/{id}/invoice", paymentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You do not have access to this invoice"));
    }

    @Test
    void updatePaymentStatus_missingStatus_returns400() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        CampaignRequest c = new CampaignRequest();
        c.setName("Status IT " + UUID.randomUUID());
        c.setBudgetRange(BudgetRange.UNDER_1K);
        MvcResult camp = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        PaymentRequest pay = new PaymentRequest();
        pay.setCampaignId(campaignId);
        pay.setInfluencerId(influencerId);
        pay.setMilestoneName("M1");
        pay.setAmount(BigDecimal.TEN);
        MvcResult payRes = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay)))
                .andExpect(status().isCreated())
                .andReturn();
        long paymentId = objectMapper.readTree(payRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/payments/{id}/status", paymentId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePaymentStatus_invalidEnum_returns400() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long influencerId = userRepository.findByEmail(SEED_INFLUENCER_EMAIL).orElseThrow().getId();

        CampaignRequest c = new CampaignRequest();
        c.setName("BadEnum IT " + UUID.randomUUID());
        c.setBudgetRange(BudgetRange.UNDER_1K);
        MvcResult camp = mockMvc.perform(post("/api/campaigns")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andReturn();
        long campaignId = objectMapper.readTree(camp.getResponse().getContentAsString()).get("id").asLong();

        PaymentRequest pay = new PaymentRequest();
        pay.setCampaignId(campaignId);
        pay.setInfluencerId(influencerId);
        pay.setMilestoneName("M1");
        pay.setAmount(BigDecimal.TEN);
        MvcResult payRes = mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pay)))
                .andExpect(status().isCreated())
                .andReturn();
        long paymentId = objectMapper.readTree(payRes.getResponse().getContentAsString()).get("id").asLong();

        Map<String, String> body = new HashMap<>();
        body.put("status", "NOT_A_STATUS");
        mockMvc.perform(put("/api/payments/{id}/status", paymentId)
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
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
