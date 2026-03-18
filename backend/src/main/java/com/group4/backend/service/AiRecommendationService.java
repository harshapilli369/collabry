package com.group4.backend.service;

import com.group4.backend.dto.InfluencerRecommendationDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiRecommendationService {
    
    private final GroqApiClient groqApiClient;
    
    public AiRecommendationService(GroqApiClient groqApiClient) {
        this.groqApiClient = groqApiClient;
    }

    public List<InfluencerRecommendationDTO> getRecommendations(Long campaignId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
