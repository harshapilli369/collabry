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

    private static final int MAX_MOCK_RECOMMENDATIONS = 3;
    private static final int SCORE_GAMING_BASE = 90;
    private static final int SCORE_GAMING_VARIANCE = 8;
    private static final int SCORE_TECH_BASE = 85;
    private static final int SCORE_TECH_VARIANCE = 10;
    private static final int SCORE_FASHION_BASE = 88;
    private static final int SCORE_FASHION_VARIANCE = 11;
    private static final int SCORE_SECONDARY_BASE = 75;
    private static final int SCORE_SECONDARY_VARIANCE = 15;
    private static final int SCORE_DEFAULT_BASE = 30;
    private static final int SCORE_DEFAULT_VARIANCE = 20;

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

        // Fall back to mock recommendations when Groq API key is not configured
        if (!groqApiClient.isConfigured()) {
            return generateMockRecommendations(campaign);
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI Matchmaker. Find the top 5 influencers for this campaign.\n\n");
        prompt.append("SCORING RULES (strict):\n");
        prompt.append("- Niche relevance is the PRIMARY factor (worth ~60% of score).\n");
        prompt.append("- An influencer whose niche directly matches the campaign topic should score 85-98.\n");
        prompt.append("- An influencer whose niche is loosely related should score 50-70.\n");
        prompt.append("- An influencer whose niche is UNRELATED (e.g. Fashion influencer for a Tech campaign) must score BELOW 40.\n");
        prompt.append("- Budget fit and rate alignment account for the remaining ~40%.\n");
        prompt.append("- Be realistic and critical. Do NOT inflate scores for unrelated niches.\n\n");
        prompt.append("Respond ONLY with a JSON object in this exact format: {\"recommendations\": [{\"influencerId\": 1, \"matchScore\": 95, \"reason\": \"string\"}]}\n\n");

        prompt.append("CAMPAIGN DETAILS:\n");
        prompt.append("Name: ").append(campaign.getName()).append("\n");
        prompt.append("Description: ").append(campaign.getDescription()).append("\n");
        prompt.append("Goal: ").append(campaign.getCampaignGoal()).append("\n");
        prompt.append("Budget Range: ").append(campaign.getBudgetRange()).append("\n\n");

        prompt.append("AVAILABLE INFLUENCERS:\n");
        for (InfluencerProfile inf : influencers) {
            prompt.append("ID: ").append(inf.getUserId())
                  .append(" | Name: ").append(inf.getName())
                  .append(" | Niche: ").append(inf.getNiche())
                  .append(" | Location: ").append(inf.getLocation())
                  .append(" | Rate: $").append(inf.getRate()).append("\n");
        }

        String groqResponse = groqApiClient.getChatCompletion(prompt.toString());

        try {
            JsonNode root = objectMapper.readTree(groqResponse);
            JsonNode recommendationsNode = root.path("recommendations");
            List<InfluencerRecommendationDTO> recs = objectMapper.convertValue(
                    recommendationsNode, new TypeReference<List<InfluencerRecommendationDTO>>() {}
            );

            // Enrich with Profile info for the frontend cards
            for (InfluencerRecommendationDTO rec : recs) {
                influencerRepository.findByUserId(rec.getInfluencerId()).ifPresent(prof -> {
                    rec.setName(prof.getName());
                    rec.setNiche(prof.getNiche());
                    rec.setProfilePictureUrl(prof.getProfilePictureUrl());
                });
            }

            // Sort by match score descending
            recs.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));

            return recs;
        } catch (Exception e) {
            e.printStackTrace();
            return generateMockRecommendations(campaign);
        }
    }

    private List<InfluencerRecommendationDTO> generateMockRecommendations(Campaign campaign) {
        List<InfluencerProfile> allInfluencers = influencerRepository.findAll();
        List<InfluencerRecommendationDTO> recs = new java.util.ArrayList<>();

        String campaignName = campaign.getName() != null ? campaign.getName().toLowerCase() : "";
        String campaignDesc = campaign.getDescription() != null ? campaign.getDescription().toLowerCase() : "";
        for (InfluencerProfile p : allInfluencers) {
            int[] scoreAndReason = calculateNicheMatchScore(p, campaignName, campaignDesc);
            InfluencerRecommendationDTO dto = buildRecommendationDTO(p, scoreAndReason[0],
                    MOCK_REASONS[scoreAndReason[1]]);
            recs.add(dto);
        }

        recs.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        return recs.subList(0, Math.min(recs.size(), MAX_MOCK_RECOMMENDATIONS));
    }

    private static final String[] MOCK_REASONS = {
        "This influencer has a steady following but their primary focus differs from your campaign.",
        "Solid secondary match. The campaign mentions their specialty, making them a great crossover candidate.",
        "Strong match due to heavy overlap in the Technology sector. Their audience converts highly on gadgets and electronics.",
        "Excellent aesthetic overlap. Their highly curated styling feeds align natively with your campaign goals.",
        "Perfect alignment. Ranked in the top 5% for Gaming audiences with extremely high engagement expected for this launch."
    };

    private int[] calculateNicheMatchScore(InfluencerProfile p, String campaignName, String campaignDesc) {
        String niche = p.getNiche() != null ? p.getNiche().toLowerCase() : "";
        if (niche.contains("gaming") && campaignName.contains("gaming")) {
            return new int[]{SCORE_GAMING_BASE + (int)(Math.random() * SCORE_GAMING_VARIANCE), 4};
        }
        if (niche.contains("technology") && campaignName.contains("tech")) {
            return new int[]{SCORE_TECH_BASE + (int)(Math.random() * SCORE_TECH_VARIANCE), 2};
        }
        if (niche.contains("fashion") && campaignName.contains("apparel")) {
            return new int[]{SCORE_FASHION_BASE + (int)(Math.random() * SCORE_FASHION_VARIANCE), 3};
        }
        if (!niche.isEmpty() && campaignDesc.contains(niche)) {
            return new int[]{SCORE_SECONDARY_BASE + (int)(Math.random() * SCORE_SECONDARY_VARIANCE), 1};
        }
        return new int[]{SCORE_DEFAULT_BASE + (int)(Math.random() * SCORE_DEFAULT_VARIANCE), 0};
    }

    private InfluencerRecommendationDTO buildRecommendationDTO(InfluencerProfile p, int score, String reason) {
        InfluencerRecommendationDTO dto = new InfluencerRecommendationDTO();
        dto.setInfluencerId(p.getUserId());
        dto.setMatchScore(score);
        dto.setReason(reason);
        dto.setName(p.getName());
        dto.setNiche(p.getNiche());
        dto.setProfilePictureUrl(p.getProfilePictureUrl());
        return dto;
    }
}
