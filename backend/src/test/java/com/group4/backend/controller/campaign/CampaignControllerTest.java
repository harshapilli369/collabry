package com.group4.backend.controller.campaign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.CampaignGoal;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.campaign.CampaignService;
import com.group4.backend.service.campaign.CampaignReportService;
import com.group4.backend.service.campaign.InvitationService;
import com.group4.backend.service.ai.AiRecommendationService;
import com.group4.backend.service.ai.GroqApiClient;
import com.group4.backend.dto.profile.InfluencerRecommendationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CampaignService campaignService;
    @MockitoBean
    private CampaignReportService campaignReportService;
    @MockitoBean
    private AiRecommendationService aiRecommendationService;
    @MockitoBean
    private InvitationService invitationService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private GroqApiClient groqApiClient;

    private User brandUser;
    private User influencerUser;

    @BeforeEach
    void setUp() {
        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);
        brandUser.setVerified(true);
        influencerUser = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencerUser.setId(20L);
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createCampaign_asBrand_returns201() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Promo");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        CampaignResponse resp = new CampaignResponse();
        resp.setId(1L);
        resp.setName("Summer Promo");
        when(campaignService.create(eq(10L), any(CampaignRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Summer Promo"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void createCampaign_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Promo");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyCampaigns_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        CampaignResponse resp = new CampaignResponse();
        resp.setId(1L);
        resp.setName("My Campaign");
        when(campaignService.findByUserId(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/campaigns/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyCampaigns_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/campaigns/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createInvitation_asBrand_returns201() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Join our campaign");
        InvitationResponse resp = new InvitationResponse();
        resp.setId(100L);
        resp.setStatus(InvitationStatus.PENDING);
        when(invitationService.createInvitation(eq(10L), eq(1L), any(InvitationRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/campaigns/1/invitations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void createInvitation_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);

        mockMvc.perform(post("/api/campaigns/1/invitations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createCampaign_invalidRequest_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        CampaignRequest request = new CampaignRequest();
        // Missing name and budgetRange, which are required

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getRecommendations_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        
        InfluencerRecommendationDTO rec = new InfluencerRecommendationDTO();
        rec.setInfluencerId(20L);
        rec.setMatchScore(98);
        rec.setReason("Great alignment.");
        
        when(aiRecommendationService.getRecommendations(1L)).thenReturn(List.of(rec));

        mockMvc.perform(get("/api/campaigns/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].influencerId").value(20))
                .andExpect(jsonPath("$[0].matchScore").value(98));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("A compelling summer campaign targeting fashion enthusiasts.");

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Summer Fashion Campaign",
                "goal", "Brand Awareness",
                "budget", "$1K-$5K"
        ));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("A compelling summer campaign targeting fashion enthusiasts."));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_missingName_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "goal", "Brand Awareness"
        ));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Campaign name is required"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void downloadCampaignReport_asBrand_returnsPdf() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        byte[] pdf = "%PDF-1.4\n".getBytes();
        when(campaignReportService.generateCampaignReportPdf(10L, 1L)).thenReturn(pdf);

        mockMvc.perform(get("/api/campaigns/1/report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"campaign-1-report.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(pdf));

        verify(campaignReportService).generateCampaignReportPdf(10L, 1L);
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void generateDescription_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Summer Campaign"
        ));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void downloadCampaignReport_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/campaigns/1/report"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_aiNotConfigured_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(groqApiClient.isConfigured()).thenReturn(false);

        String body = objectMapper.writeValueAsString(java.util.Map.of("name", "My Campaign"));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("AI service is not configured"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_aiReturnsQuotedDescription_stripsQuotes() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("\"A quoted campaign description.\"");

        String body = objectMapper.writeValueAsString(java.util.Map.of("name", "My Campaign"));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("A quoted campaign description."));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_aiThrowsException_returns500() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenThrow(new RuntimeException("AI service error"));

        String body = objectMapper.writeValueAsString(java.util.Map.of("name", "My Campaign"));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to generate description: AI service error"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_withOnlyName_coversEmptyGoalAndBudgetBranches() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("A campaign description.");

        String body = objectMapper.writeValueAsString(java.util.Map.of("name", "My Campaign"));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("A campaign description."));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getRecommendations_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/campaigns/1/recommendations"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unverified@test.com")
    void createCampaign_unverifiedBrand_returns201() throws Exception {
        User unverifiedBrand = new User("unverified@test.com", "pass", Role.BRAND);
        unverifiedBrand.setId(30L);
        // isVerified() defaults to false
        when(currentUserProvider.getCurrentUser()).thenReturn(unverifiedBrand);
        
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Promo");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        CampaignResponse resp = new CampaignResponse();
        resp.setId(2L);
        resp.setName("Summer Promo");
        when(campaignService.create(eq(30L), any(CampaignRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void createCampaign_userNotFound_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenThrow(new IllegalArgumentException("User not found"));
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Promo");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
