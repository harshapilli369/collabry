package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.RatingRequest;
import com.group4.backend.dto.RatingResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingController.class)
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RatingService ratingService;
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
    void submitRating_asBrand_returns201() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        request.setReview("Excellent work!");
        RatingResponse response = new RatingResponse();
        response.setId(1L);
        response.setRating(5);
        response.setReview("Excellent work!");
        when(ratingService.submitRating(eq(10L), any(RatingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/ratings").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.review").value("Excellent work!"));
    }

    @Test
    @WithMockUser(username = "influencer@test.com")
    void submitRating_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(influencerUser));
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);

        mockMvc.perform(post("/api/ratings").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void submitRating_invalidBody_returns400() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        mockMvc.perform(post("/api/ratings").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "unknown@test.com")
    void submitRating_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        RatingRequest request = new RatingRequest();
        request.setInvitationId(100L);
        request.setRating(5);
        mockMvc.perform(post("/api/ratings").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
