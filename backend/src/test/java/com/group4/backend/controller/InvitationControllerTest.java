package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.*;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.InvitationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvitationController.class)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private InvitationService invitationService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtUtils jwtUtils;

    private User influencerUser;
    private User brandUser;

    @BeforeEach
    void setUp() {
        influencerUser = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencerUser.setId(20L);
        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyInvitations_asInfluencer_returns200WithList() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        InvitationResponse resp = new InvitationResponse();
        resp.setId(1L);
        resp.setStatus(InvitationStatus.PENDING);
        when(invitationService.getInvitationsForInfluencer(20L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/invitations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyInvitations_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        mockMvc.perform(get("/api/invitations/me")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getInvitationById_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        mockMvc.perform(get("/api/invitations/100")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getInvitationById_asInfluencer_returns200WithCampaignDetails() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        InvitationDetailResponse detail = new InvitationDetailResponse();
        detail.setId(100L);
        detail.setStatus(InvitationStatus.PENDING);
        CampaignResponse campaign = new CampaignResponse();
        campaign.setId(1L);
        campaign.setName("Test Campaign");
        detail.setCampaign(campaign);
        when(invitationService.getInvitationWithCampaignDetails(100L, 20L)).thenReturn(detail);

        mockMvc.perform(get("/api/invitations/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.campaign.name").value("Test Campaign"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void respond_accept_asInfluencer_returns200() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");
        InvitationResponse resp = new InvitationResponse();
        resp.setStatus(InvitationStatus.ACCEPTED);
        when(invitationService.respond(eq(100L), eq(20L), any(RespondRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/invitations/100/respond").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void respond_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");
        mockMvc.perform(post("/api/invitations/100/respond").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void negotiate_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        NegotiationRequest request = new NegotiationRequest();
        request.setProposedAmount(java.math.BigDecimal.valueOf(500));
        mockMvc.perform(put("/api/invitations/100/negotiate").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void negotiate_asInfluencer_returns200() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        NegotiationRequest request = new NegotiationRequest();
        request.setProposedAmount(java.math.BigDecimal.valueOf(500));
        InvitationResponse resp = new InvitationResponse();
        resp.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationService.negotiate(eq(100L), eq(20L), any(NegotiationRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/invitations/100/negotiate").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEGOTIATING"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void confirmTerms_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        mockMvc.perform(post("/api/invitations/100/confirm-terms").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void confirmTerms_asBrand_returns200() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InvitationResponse resp = new InvitationResponse();
        resp.setStatus(InvitationStatus.CONFIRMED);
        when(invitationService.confirmTerms(100L, 10L)).thenReturn(resp);

        mockMvc.perform(post("/api/invitations/100/confirm-terms").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // --- TDD: Brand sent invitations, withdraw, edit ---

    @Test
    @WithMockUser(username = "brand@test.com")
    void getSentInvitations_asBrand_returns200WithList() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InvitationResponse resp = new InvitationResponse();
        resp.setId(1L);
        resp.setStatus(InvitationStatus.PENDING);
        resp.setCampaignId(5L);
        resp.setInfluencerId(20L);
        when(invitationService.getInvitationsForBrand(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/invitations/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getSentInvitations_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        mockMvc.perform(get("/api/invitations/sent")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyInvitationsAsBrand_returns200() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InvitationResponse resp = new InvitationResponse();
        resp.setId(2L);
        when(invitationService.getInvitationsForBrand(10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/invitations/brand/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyInvitationsAsBrand_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        mockMvc.perform(get("/api/invitations/brand/me")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void withdrawInvitation_asBrand_returns204() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));

        mockMvc.perform(delete("/api/invitations/100").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void withdrawInvitation_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        mockMvc.perform(delete("/api/invitations/100").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateInvitation_asBrand_returns200WithUpdatedResponse() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        UpdateInvitationRequest request = new UpdateInvitationRequest();
        request.setMessage("Updated message");
        request.setProposedAmount(java.math.BigDecimal.valueOf(750));
        InvitationResponse resp = new InvitationResponse();
        resp.setId(100L);
        resp.setStatus(InvitationStatus.PENDING);
        resp.setBrandMessage("Updated message");
        when(invitationService.updateInvitation(eq(100L), eq(10L), any(UpdateInvitationRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/api/invitations/100").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateInvitation_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        mockMvc.perform(put("/api/invitations/100").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void getMyInvitations_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/invitations/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
