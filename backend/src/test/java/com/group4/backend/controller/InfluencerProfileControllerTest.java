package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.InfluencerProfileRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InfluencerProfileController.class)
class InfluencerProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private InfluencerProfileService influencerProfileService;
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
    void getMyProfile_asInfluencerWithProfile_returns200() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        InfluencerProfileResponse response = new InfluencerProfileResponse();
        response.setId(2L);
        response.setUserId(20L);
        response.setName("Jane Doe");
        response.setNiche("Fashion");
        response.setComplete(true);
        when(influencerProfileService.getByUserId(20L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userId").value(20))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.complete").value(true));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyProfile_asInfluencerNoProfile_returns404() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        when(influencerProfileService.getByUserId(20L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyProfile_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateMyProfile_asInfluencer_returns200() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        InfluencerProfileRequest request = new InfluencerProfileRequest();
        request.setName("Jane Doe");
        request.setAge(25);
        request.setLocation("NYC");
        request.setNiche("Fashion");
        request.setInstagramHandle("jane_doe");
        request.setRate(BigDecimal.valueOf(500));
        request.setSaveAsDraft(false);
        InfluencerProfileResponse response = new InfluencerProfileResponse();
        response.setUserId(20L);
        response.setName("Jane Doe");
        response.setComplete(true);
        when(influencerProfileService.createOrUpdateForUser(eq(20L), any(InfluencerProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/influencers/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.complete").value(true));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateMyProfile_asBrand_returns403() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InfluencerProfileRequest request = new InfluencerProfileRequest();
        request.setName("Jane");
        request.setAge(25);
        request.setLocation("NYC");
        request.setNiche("Fashion");

        mockMvc.perform(put("/api/influencers/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateMyProfile_serviceThrowsIllegalArg_returns400() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        InfluencerProfileRequest request = new InfluencerProfileRequest();
        request.setName("Jane");
        request.setAge(25);
        request.setLocation("NYC");
        request.setNiche("Fashion");
        request.setSaveAsDraft(false);
        request.setRate(BigDecimal.valueOf(500));
        when(influencerProfileService.createOrUpdateForUser(eq(20L), any(InfluencerProfileRequest.class)))
                .thenThrow(new IllegalArgumentException("At least one social media handle is required to complete your profile"));

        mockMvc.perform(put("/api/influencers/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one social media handle is required to complete your profile"));
    }
}
