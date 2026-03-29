package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.CampaignRequest;
import com.group4.backend.dto.CampaignResponse;
import com.group4.backend.dto.InvitationRequest;
import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.CampaignGoal;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.CampaignService;
import com.group4.backend.service.InvitationService;
import com.group4.backend.service.AiRecommendationService;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import com.group4.backend.service.InvitationService;
import com.group4.backend.service.AiRecommendationService;
import com.group4.backend.service.GroqApiClient;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
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
    @MockBean
    private CampaignService campaignService;
    @MockBean
    private AiRecommendationService aiRecommendationService;
    @MockBean
    private InvitationService invitationService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtUtils jwtUtils;
    @MockBean
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));

        mockMvc.perform(get("/api/campaigns/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void createInvitation_asBrand_returns201() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));

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
    @WithMockUser(username = "influencer@test.com")
    void generateDescription_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Summer Campaign"
        ));

        mockMvc.perform(post("/api/campaigns/generate-description").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void generateDescription_aiNotConfigured_returns400() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
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
    @WithMockUser(username = "unverified@test.com")
    void createCampaign_unverifiedBrand_returns403() throws Exception {
        User unverifiedBrand = new User("unverified@test.com", "pass", Role.BRAND);
        unverifiedBrand.setId(30L);
        // isVerified() defaults to false
        when(userRepository.findByEmail("unverified@test.com")).thenReturn(Optional.of(unverifiedBrand));
        CampaignRequest request = new CampaignRequest();
        request.setName("Summer Promo");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        mockMvc.perform(post("/api/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void createCampaign_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
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
