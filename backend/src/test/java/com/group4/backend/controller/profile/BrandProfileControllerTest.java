package com.group4.backend.controller.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.dto.profile.BrandProfileResponse;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.profile.BrandProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandProfileController.class)
class BrandProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private BrandProfileService brandProfileService;
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
    void getMyProfile_asBrandWithProfile_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        BrandProfileResponse response = new BrandProfileResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setName("Acme Inc");
        response.setIndustry("Fashion");
        when(brandProfileService.getByUserId(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/brands/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.name").value("Acme Inc"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyProfile_asBrandNoProfile_returns404() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(brandProfileService.getByUserId(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/brands/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyProfile_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/brands/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateMyProfile_asBrand_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        BrandProfileRequest request = new BrandProfileRequest();
        request.setName("Acme Inc");
        request.setIndustry("Fashion");
        request.setWebsite("https://acme.com");
        request.setEmail("contact@acme.com");
        BrandProfileResponse response = new BrandProfileResponse();
        response.setUserId(10L);
        response.setName("Acme Inc");
        when(brandProfileService.createOrUpdateForUser(eq(10L), any(BrandProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/brands/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Inc"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateMyProfile_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        BrandProfileRequest request = new BrandProfileRequest();
        request.setName("Acme");
        request.setIndustry("Tech");
        request.setWebsite("https://acme.com");
        request.setEmail("a@a.com");

        mockMvc.perform(put("/api/brands/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBrandProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/brands/10/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getBrandProfile_publicProfileFound_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        BrandProfileResponse response = new BrandProfileResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setName("Acme Inc");
        response.setBudgetRange(BudgetRange.ONE_K_5K);
        when(brandProfileService.getPublicProfile(10L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/brands/10/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.name").value("Acme Inc"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getBrandProfile_publicProfileNotFound_returns404() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(brandProfileService.getPublicProfile(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/brands/99/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateMyProfile_validationFails_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        // Missing required fields
        mockMvc.perform(put("/api/brands/me").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"industry\":\"\",\"website\":\"\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void getMyProfile_userNotFound_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenThrow(new IllegalArgumentException("User not found"));
        mockMvc.perform(get("/api/brands/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
