package com.group4.backend.controller.campaign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.DeliverableUpdateRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.campaign.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollaborationsController.class)
class CollaborationsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private InvitationService invitationService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
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
    void getCollaborationHistory_asInfluencer_returns200WithList() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        InvitationResponse resp = new InvitationResponse();
        resp.setId(1L);
        resp.setStatus(InvitationStatus.ACCEPTED);
        when(invitationService.getCollaborationHistory(20L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/collaborations/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getCollaborationHistory_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        mockMvc.perform(get("/api/collaborations/me")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void getCollaborationHistory_userNotFound_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenThrow(new IllegalArgumentException("User not found"));
        mockMvc.perform(get("/api/collaborations/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // --- updateDeliverable tests ---

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateDeliverable_asInfluencer_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        InvitationResponse resp = new InvitationResponse();
        resp.setId(1L);
        resp.setDeliverableStatus("SUBMITTED");
        when(invitationService.updateDeliverableStatus(eq(1L), eq(20L), any(DeliverableUpdateRequest.class)))
                .thenReturn(resp);

        DeliverableUpdateRequest request = new DeliverableUpdateRequest();
        request.setDeliverableStatus("SUBMITTED");

        mockMvc.perform(put("/api/collaborations/1/deliverable")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliverableStatus").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateDeliverable_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(put("/api/collaborations/1/deliverable")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliverableStatus\": \"SUBMITTED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only influencers can update deliverables"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateDeliverable_serviceThrows_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(invitationService.updateDeliverableStatus(eq(1L), eq(20L), any(DeliverableUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invitation not found"));

        mockMvc.perform(put("/api/collaborations/1/deliverable")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliverableStatus\": \"SUBMITTED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invitation not found"));
    }

    // --- approveDeliverable tests ---

    @Test
    @WithMockUser(username = "brand@test.com")
    void approveDeliverable_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        InvitationResponse resp = new InvitationResponse();
        resp.setId(1L);
        resp.setDeliverableStatus("APPROVED");
        when(invitationService.approveDeliverable(1L, 10L)).thenReturn(resp);

        mockMvc.perform(put("/api/collaborations/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deliverableStatus").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void approveDeliverable_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(put("/api/collaborations/1/approve")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only brands can approve deliverables"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void approveDeliverable_serviceThrows_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(invitationService.approveDeliverable(1L, 10L))
                .thenThrow(new IllegalArgumentException("Only submitted deliverables can be approved"));

        mockMvc.perform(put("/api/collaborations/1/approve")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only submitted deliverables can be approved"));
    }
}
