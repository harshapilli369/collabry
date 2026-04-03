package com.group4.backend.controller.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.profile.CollaborationAvailabilityRequest;
import com.group4.backend.dto.profile.InfluencerProfileRequest;
import com.group4.backend.dto.profile.InfluencerProfileResponse;
import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.ai.GroqApiClient;
import com.group4.backend.service.profile.InfluencerProfileService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Merged: influencer profile CRUD (feature) + brand search (develop).
 */
@WebMvcTest(InfluencerProfileController.class)
class InfluencerProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private InfluencerProfileService influencerProfileService;
    @MockitoBean
    private CurrentUserProvider currentUserProvider;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private GroqApiClient groqApiClient;

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
    @WithMockUser(username = "brand@test.com")
    void search_asBrand_returns200AndList() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        InfluencerProfileResponse resp = new InfluencerProfileResponse();
        resp.setId(1L);
        resp.setUserId(20L);
        resp.setName("Jane");
        resp.setNiche("Fashion");
        resp.setLocation("NYC");
        resp.setComplete(true);
        when(influencerProfileService.search(any(InfluencerSearchFilter.class))).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/influencers/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Jane"))
                .andExpect(jsonPath("$[0].niche").value("Fashion"));

        verify(influencerProfileService).search(new InfluencerSearchFilter(null, null, null, null, null, null));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void search_asBrand_withQueryParams_passesParamsToService() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(influencerProfileService.search(new InfluencerSearchFilter("Fashion", "NYC", 1000L, 100000L, BigDecimal.valueOf(2.5), null)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/influencers/search")
                        .param("niche", "Fashion")
                        .param("location", "NYC")
                        .param("minFollowers", "1000")
                        .param("maxFollowers", "100000")
                        .param("minEngagementRate", "2.5"))
                .andExpect(status().isOk());

        verify(influencerProfileService).search(new InfluencerSearchFilter("Fashion", "NYC", 1000L, 100000L, BigDecimal.valueOf(2.5), null));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void search_asBrand_withAvailableOnly_passesToService() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(influencerProfileService.search(new InfluencerSearchFilter(null, null, null, null, null, true))).thenReturn(List.of());

        mockMvc.perform(get("/api/influencers/search").param("availableOnly", "true"))
                .andExpect(status().isOk());

        verify(influencerProfileService).search(new InfluencerSearchFilter(null, null, null, null, null, true));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void search_asInfluencer_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(get("/api/influencers/search"))
                .andExpect(status().isForbidden());

        verify(influencerProfileService, never()).search(any(InfluencerSearchFilter.class));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void search_withMinFollowersGreaterThanMaxFollowers_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        when(influencerProfileService.search(new InfluencerSearchFilter(null, null, 10000L, 1000L, null, null)))
                .thenThrow(new IllegalArgumentException("minFollowers cannot be greater than maxFollowers"));

        mockMvc.perform(get("/api/influencers/search")
                        .param("minFollowers", "10000")
                        .param("maxFollowers", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minFollowers cannot be greater than maxFollowers"));

        verify(influencerProfileService).search(new InfluencerSearchFilter(null, null, 10000L, 1000L, null, null));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyProfile_asInfluencerWithProfile_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
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
    void updateCollaborationAvailability_asInfluencer_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        InfluencerProfileResponse response = new InfluencerProfileResponse();
        response.setUserId(20L);
        response.setName("Jane");
        response.setOpenToCollaborations(false);
        response.setComplete(true);
        when(influencerProfileService.updateCollaborationAvailability(20L, false)).thenReturn(response);

        CollaborationAvailabilityRequest body = new CollaborationAvailabilityRequest();
        body.setOpenToCollaborations(false);

        mockMvc.perform(put("/api/influencers/me/collaboration-availability").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openToCollaborations").value(false));

        verify(influencerProfileService).updateCollaborationAvailability(20L, false);
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void updateCollaborationAvailability_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
        CollaborationAvailabilityRequest body = new CollaborationAvailabilityRequest();
        body.setOpenToCollaborations(true);

        mockMvc.perform(put("/api/influencers/me/collaboration-availability").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        verify(influencerProfileService, never()).updateCollaborationAvailability(anyLong(), anyBoolean());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void getMyProfile_asInfluencerNoProfile_returns404() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(influencerProfileService.getByUserId(20L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void getMyProfile_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void updateMyProfile_asInfluencer_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
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
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);
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
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
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

    @Test
    @WithMockUser(username = "influencer@test.com")
    void enhanceBio_asInfluencer_returns200() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("Enhanced bio text.");

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Original bio text."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enhancedBio").value("Enhanced bio text."));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void enhanceBio_quotedResponse_stripsQuotes() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenReturn("\"Quoted bio text.\"");

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Original bio text."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enhancedBio").value("Quoted bio text."));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void enhanceBio_missingBio_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("other", "value"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bio text is required"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void enhanceBio_aiNotConfigured_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(groqApiClient.isConfigured()).thenReturn(false);

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Original bio."))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("AI service is not configured"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void enhanceBio_aiThrowsException_returns500() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(influencerUser);
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getTextCompletion(anyString())).thenThrow(new RuntimeException("AI error"));

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Original bio."))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to enhance bio: AI error"));
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void enhanceBio_asBrand_returns403() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(brandUser);

        mockMvc.perform(post("/api/influencers/enhance-bio").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("bio", "Some bio"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void getMyProfile_userNotFound_returns400() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenThrow(new IllegalArgumentException("User not found"));
        mockMvc.perform(get("/api/influencers/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
