package com.group4.backend.controller;

import com.group4.backend.dto.InvitationResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollaborationsController.class)
class CollaborationsControllerTest {

    @Autowired
    private MockMvc mockMvc;
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
    void getCollaborationHistory_asInfluencer_returns200WithList() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
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
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        mockMvc.perform(get("/api/collaborations/me")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void getCollaborationHistory_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/collaborations/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
