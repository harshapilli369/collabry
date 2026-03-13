package com.group4.backend.controller;

import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.InfluencerProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD-style tests for InfluencerProfileController.
 * Search endpoint: GET /api/influencers/search (brand only).
 */
@WebMvcTest(InfluencerProfileController.class)
class InfluencerProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InfluencerProfileService influencerProfileService;
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
    void search_asBrand_returns200AndList() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InfluencerProfileResponse resp = new InfluencerProfileResponse();
        resp.setId(1L);
        resp.setUserId(20L);
        resp.setName("Jane");
        resp.setNiche("Fashion");
        resp.setLocation("NYC");
        resp.setComplete(true);
        when(influencerProfileService.search(any(), any(), any(), any(), any())).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/influencers/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane"))
                .andExpect(jsonPath("$[0].niche").value("Fashion"));

        verify(influencerProfileService).search(null, null, null, null, null);
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void search_asBrand_withQueryParams_passesParamsToService() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        when(influencerProfileService.search("Fashion", "NYC", 1000L, 100000L, java.math.BigDecimal.valueOf(2.5)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/influencers/search")
                        .param("niche", "Fashion")
                        .param("location", "NYC")
                        .param("minFollowers", "1000")
                        .param("maxFollowers", "100000")
                        .param("minEngagementRate", "2.5"))
                .andExpect(status().isOk());

        verify(influencerProfileService).search("Fashion", "NYC", 1000L, 100000L, java.math.BigDecimal.valueOf(2.5));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void search_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));

        mockMvc.perform(get("/api/influencers/search"))
                .andExpect(status().isForbidden());

        verify(influencerProfileService, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void search_withMinFollowersGreaterThanMaxFollowers_returns400() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        // Service validates and throws; controller handler returns 400
        when(influencerProfileService.search(any(), any(), eq(10000L), eq(1000L), any()))
                .thenThrow(new IllegalArgumentException("minFollowers cannot be greater than maxFollowers"));

        mockMvc.perform(get("/api/influencers/search")
                        .param("minFollowers", "10000")
                        .param("maxFollowers", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minFollowers cannot be greater than maxFollowers"));

        verify(influencerProfileService).search(null, null, 10000L, 1000L, null);
    }
}
