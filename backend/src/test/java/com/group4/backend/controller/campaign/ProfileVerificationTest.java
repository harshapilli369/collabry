package com.group4.backend.controller.campaign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.ai.AiRecommendationService;
import com.group4.backend.service.campaign.CampaignService;
import com.group4.backend.service.campaign.CampaignReportService;
import com.group4.backend.service.ai.GroqApiClient;
import com.group4.backend.service.campaign.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

    @MockitoBean
    private CampaignService campaignService;
    @MockitoBean
    private CampaignReportService campaignReportService;

    @MockitoBean
    private AiRecommendationService aiRecommendationService;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private GroqApiClient groqApiClient;

    private User unverifiedBrand;

    @BeforeEach
    void setUp() {
        unverifiedBrand = new User("unverified@test.com", "password", Role.BRAND);
        unverifiedBrand.setId(1L);
        unverifiedBrand.setVerified(false);
    }

    @Test
    @WithMockUser(username = "unverified@test.com")
    void createCampaign_asUnverifiedBrand_returns201() throws Exception {
        when(currentUserProvider.getCurrentUser()).thenReturn(unverifiedBrand);

        CampaignRequest request = new CampaignRequest();
        request.setName("New Campaign");
        request.setBudgetRange(BudgetRange.ONE_K_5K);

        mockMvc.perform(post("/api/campaigns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
