package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.CampaignRequest;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.AiRecommendationService;
import com.group4.backend.service.CampaignService;
import com.group4.backend.service.GroqApiClient;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampaignController.class)
public class ProfileVerificationTest {

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

    private User unverifiedBrand;

    @BeforeEach
    void setUp() {
        unverifiedBrand = new User("unverified@test.com", "password", Role.BRAND);
        unverifiedBrand.setId(1L);
        // Note: isVerified is not yet in the User model, but we will add it in the GREEN phase.
        // For the RED phase, the controller naturally won't check it, so the test will fail
        // because we expect 403 but get 201.
    }

    @Test
    @WithMockUser(username = "unverified@test.com")
    void createCampaign_asUnverifiedBrand_returns403() throws Exception {
        when(userRepository.findByEmail("unverified@test.com")).thenReturn(Optional.of(unverifiedBrand));

        CampaignRequest request = new CampaignRequest();
        request.setName("New Campaign");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        // This should fail (fail to return 403) because isVerified check isn't implemented.
        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
