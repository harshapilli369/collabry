package com.group4.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.repository.CampaignRepository;
import com.group4.backend.repository.InfluencerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiRecommendationService {

    private final GroqApiClient groqApiClient;
    private final ObjectMapper objectMapper;
    private final CampaignRepository campaignRepository;
    private final InfluencerProfileRepository influencerRepository;

    public AiRecommendationService(GroqApiClient groqApiClient, ObjectMapper objectMapper,
                                   CampaignRepository campaignRepository, InfluencerProfileRepository influencerRepository) {
        this.groqApiClient = groqApiClient;
        this.objectMapper = objectMapper;
        this.campaignRepository = campaignRepository;
        this.influencerRepository = influencerRepository;
    }

    public List<InfluencerRecommendationDTO> getRecommendations(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        List<InfluencerProfile> influencers = influencerRepository.findAll();
        if (influencers.isEmpty()) return Collections.emptyList();

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI Matchmaker. Find the top 5 influencers for this campaign. ");
        prompt.append("Respond ONLY with a JSON object in this exact format: {\"recommendations\": [{\"influencerId\": 1, \"matchScore\": 95, \"reason\": \"string\"}]}\n\n");
        
        prompt.append("CAMPAIGN DETAILS:\n");
        prompt.append("Name: ").append(campaign.getName()).append("\n");
        prompt.append("Description: ").append(campaign.getDescription()).append("\n");
        prompt.append("Goal: ").append(campaign.getCampaignGoal()).append("\n");
        prompt.append("Budget Range: ").append(campaign.getBudgetRange()).append("\n\n");

        prompt.append("AVAILABLE INFLUENCERS:\n");
        for (InfluencerProfile inf : influencers) {
            prompt.append("ID: ").append(inf.getId())
                  .append(" | Name: ").append(inf.getName())
                  .append(" | Niche: ").append(inf.getNiche())
                  .append(" | Location: ").append(inf.getLocation())
                  .append(" | Rate: $").append(inf.getRate()).append("\n");
        }

        String groqResponse = groqApiClient.getChatCompletion(prompt.toString());

        try {
            JsonNode root = objectMapper.readTree(groqResponse);
            JsonNode recommendationsNode = root.path("recommendations");
            return objectMapper.convertValue(recommendationsNode, new TypeReference<List<InfluencerRecommendationDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
