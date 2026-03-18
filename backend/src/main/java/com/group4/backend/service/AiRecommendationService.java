package com.group4.backend.service;

import com.group4.backend.dto.InfluencerRecommendationDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class AiRecommendationService {
    
    private final GroqApiClient groqApiClient;
    private final ObjectMapper objectMapper;
    
    public AiRecommendationService(GroqApiClient groqApiClient, ObjectMapper objectMapper) {
        this.groqApiClient = groqApiClient;
        this.objectMapper = objectMapper;
    }

    public List<InfluencerRecommendationDTO> getRecommendations(Long campaignId) {
        // Construct a prompt for the specific campaign
        String prompt = "Give me recommendations for campaign " + campaignId;
        
        // Fetch raw JSON string from Groq
        String groqResponse = groqApiClient.getChatCompletion(prompt);
        
        try {
            // Parse the JSON array into a List of DTOs
            return objectMapper.readValue(groqResponse, new TypeReference<List<InfluencerRecommendationDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
