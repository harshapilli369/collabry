package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.InvitationRequest;
import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.CampaignService;
import com.group4.backend.service.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private InvitationService invitationService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
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
}
