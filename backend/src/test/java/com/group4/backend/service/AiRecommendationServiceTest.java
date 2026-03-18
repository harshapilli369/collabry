package com.group4.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.repository.CampaignRepository;
import com.group4.backend.repository.InfluencerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AiRecommendationServiceTest {

    @Mock
    private GroqApiClient groqApiClient;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiRecommendationService aiRecommendationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRecommendations_ShouldReturnParsedInfluencers() {
        // Arrange
        Long campaignId = 1L;
        
        Campaign dummyCampaign = new Campaign();
        dummyCampaign.setId(campaignId);
        dummyCampaign.setName("Test Campaign");
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(dummyCampaign));
        
        InfluencerProfile dummyInfluencer = new InfluencerProfile();
        dummyInfluencer.setId(10L);
        dummyInfluencer.setName("Alex");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(dummyInfluencer));
        
        String mockGroqResponse = "{\"recommendations\": [{\"influencerId\": 10, \"matchScore\": 95, \"reason\": \"Perfect match.\"}]}";
        when(groqApiClient.getChatCompletion(anyString())).thenReturn(mockGroqResponse);

        // Act
        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(campaignId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return exactly 1 recommendation");
        assertEquals(10L, result.get(0).getInfluencerId());
        assertEquals(95, result.get(0).getMatchScore());
        assertEquals("Perfect match.", result.get(0).getReason());
    }
}
