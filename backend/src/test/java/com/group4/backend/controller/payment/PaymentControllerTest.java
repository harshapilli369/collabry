package com.group4.backend.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.payment.PaymentRequest;
import com.group4.backend.dto.payment.PaymentResponse;
import com.group4.backend.model.PaymentStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
    private JwtUtils jwtUtils;

    private User brandUser;
    private User influencerUser;

    @BeforeEach
    void setUp() {
        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);
        influencerUser = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencerUser.setId(20L);
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createPayment_asBrand_returns201() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(20L);
        request.setMilestoneName("Delivery");
        request.setAmount(new BigDecimal("100.00"));

        PaymentResponse resp = new PaymentResponse();
        resp.setId(5L);
        resp.setStatus(PaymentStatus.PENDING);
        when(paymentService.createPayment(eq(10L), any(PaymentRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/payments").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void createPayment_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(20L);
        request.setMilestoneName("Delivery");
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/payments").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createPayment_invalidRequest_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        PaymentRequest request = new PaymentRequest();
        // Missing required fields

        mockMvc.perform(post("/api/payments").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyPayments_asInfluencer_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        PaymentResponse resp = new PaymentResponse();
        resp.setId(5L);
        when(paymentService.getPaymentsForInfluencer(20L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/payments/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyPayments_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(get("/api/payments/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateStatus_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        PaymentResponse resp = new PaymentResponse();
        resp.setId(5L);
        resp.setStatus(PaymentStatus.PAID);
        when(paymentService.updatePaymentStatus(5L, PaymentStatus.PAID, 10L)).thenReturn(resp);

        mockMvc.perform(put("/api/payments/5/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PAID"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateStatus_invalidStatus_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(put("/api/payments/5/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INVALID_STATUS"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateStatus_missingStatusKey_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(put("/api/payments/5/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateStatus_lowercaseStatus_parsesWithValueOf() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        PaymentResponse resp = new PaymentResponse();
        resp.setId(5L);
        resp.setStatus(PaymentStatus.PAID);
        when(paymentService.updatePaymentStatus(5L, PaymentStatus.PAID, 10L)).thenReturn(resp);

        mockMvc.perform(put("/api/payments/5/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "paid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateStatus_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(put("/api/payments/5/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "PAID"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getPaymentsForCampaign_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        PaymentResponse resp = new PaymentResponse();
        resp.setId(1L);
        when(paymentService.getPaymentsForCampaign(3L, 10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/payments/campaign/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getPaymentsForCampaign_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/payments/campaign/3"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getDelayedPayments_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(paymentService.getDelayedPayments(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/delayed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getDelayedPayments_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/payments/delayed"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getInvoice_isAllowed() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        PaymentResponse resp = new PaymentResponse();
        resp.setId(5L);
        resp.setInvoiceNumber("INV-ABCD");
        when(paymentService.getInvoice(5L, 20L)).thenReturn(resp);

        mockMvc.perform(get("/api/payments/5/invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-ABCD"));
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void createPayment_userNotFound_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenThrow(new IllegalArgumentException("User not found"));
        PaymentRequest request = new PaymentRequest();
        request.setCampaignId(1L);
        request.setInfluencerId(20L);
        request.setMilestoneName("Delivery");
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/payments").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
