package com.group4.backend.service;

import com.group4.backend.dto.InfluencerRecommendationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AiRecommendationServiceTest {

    @Mock
    private GroqApiClient groqApiClient;

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
        String mockGroqResponse = "[{\"influencerId\": 10, \"matchScore\": 95, \"reason\": \"Perfect match.\"}]";
        
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
