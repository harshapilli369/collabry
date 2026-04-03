package com.group4.backend.service;
import com.group4.backend.service.ai.AiRecommendationService;
import com.group4.backend.service.ai.GroqApiClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.profile.InfluencerRecommendationDTO;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Test Campaign");
        campaign.setDescription("A test");
        lenient().when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
    }

    @Test
    void getRecommendations_parsesGroqJsonAndReturnsRecommendations() {
        InfluencerProfile inf = profile(10L, "Alex", "Lifestyle", "NYC");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(true);
        String json = "{\"recommendations\": [{\"influencerId\": 10, \"matchScore\": 95, \"reason\": \"Perfect match.\"}]}";
        when(groqApiClient.getChatCompletion(anyString())).thenReturn(json);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getInfluencerId()).as("influencer id").isEqualTo(10L),
                () -> assertThat(result.get(0).getMatchScore()).as("match score").isEqualTo(95),
                () -> assertThat(result.get(0).getReason()).as("reason").isEqualTo("Perfect match.")
        );
    }

    @Test
    void getRecommendations_campaignNotFound_throws() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiRecommendationService.getRecommendations(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Campaign not found");
    }

    @Test
    void getRecommendations_emptyInfluencers_returnsEmptyWithoutCallingGroq() {
        when(influencerProfileRepository.findAll()).thenReturn(List.of());

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertThat(result).isEmpty();
        verify(groqApiClient, never()).isConfigured();
        verify(groqApiClient, never()).getChatCompletion(anyString());
    }

    @Test
    void getRecommendations_groqNotConfigured_usesMockGenerator() {
        InfluencerProfile inf = profile(10L, "Alex", "Cooking", "LA");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result not empty").isNotEmpty(),
                () -> assertThat(result.get(0).getInfluencerId()).as("influencer id").isEqualTo(10L),
                () -> assertThat(result.get(0).getName()).as("name").isEqualTo("Alex")
        );
        verify(groqApiClient, never()).getChatCompletion(anyString());
    }

    @Test
    void getRecommendations_mockGamingBranch_whenCampaignAndNicheContainGaming() {
        campaign.setName("Epic gaming launch");
        InfluencerProfile inf = profile(20L, "Gamer", "PC gaming", "US");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(90, 97),
                () -> assertThat(result.get(0).getReason()).as("reason contains Gaming").contains("Gaming")
        );
    }

    @Test
    void getRecommendations_mockTechBranch_whenCampaignAndNicheAlign() {
        campaign.setName("Best tech gadgets 2025");
        InfluencerProfile inf = profile(21L, "Techie", "technology reviews", "UK");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(85, 94),
                () -> assertThat(result.get(0).getReason()).as("reason contains Technology").contains("Technology")
        );
    }

    @Test
    void getRecommendations_mockFashionBranch_whenApparelCampaign() {
        campaign.setName("Summer apparel collection");
        InfluencerProfile inf = profile(22L, "Style", "high fashion", "Paris");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(88, 98),
                () -> assertThat(result.get(0).getReason()).as("reason contains aesthetic").contains("aesthetic")
        );
    }

    @Test
    void getRecommendations_mockDescriptionContainsNiche_secondaryMatch() {
        campaign.setName("Generic promo");
        campaign.setDescription("We need fitness creators for this wellness push");
        InfluencerProfile inf = profile(23L, "Fit", "fitness", "Miami");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(75, 89),
                () -> assertThat(result.get(0).getReason()).as("reason contains crossover").contains("crossover")
        );
    }

    @Test
    void getRecommendations_mockBaseline_whenNoKeywordMatch() {
        campaign.setName("Office supplies");
        campaign.setDescription("Paper and pens");
        InfluencerProfile inf = profile(24L, "Creator", "astronomy", "TX");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(30, 49),
                () -> assertThat(result.get(0).getReason()).as("reason").contains("differs from your campaign")
        );
    }

    @Test
    void getRecommendations_mockHandlesNullCampaignNameDescriptionAndNiche() {
        campaign.setName(null);
        campaign.setDescription(null);
        InfluencerProfile inf = new InfluencerProfile();
        inf.setUserId(30L);
        inf.setName("X");
        inf.setNiche(null);
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getMatchScore()).as("match score range").isBetween(30, 49)
        );
    }

    @Test
    void getRecommendations_mockReturnsTopThreeOnly() {
        List<InfluencerProfile> many = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            many.add(profile(100L + i, "Inf" + i, "misc", "City"));
        }
        when(influencerProfileRepository.findAll()).thenReturn(many);
        when(groqApiClient.isConfigured()).thenReturn(false);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertThat(result).hasSize(3);
    }

    @Test
    void getRecommendations_groqInvalidJson_fallsBackToMock() {
        InfluencerProfile inf = profile(10L, "Alex", "misc", "NYC");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getChatCompletion(anyString())).thenReturn("not valid json {{{");

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("fallback result not empty").isNotEmpty(),
                () -> assertThat(result.get(0).getInfluencerId()).as("influencer id").isEqualTo(10L)
        );
    }

    @Test
    void getRecommendations_groqSuccess_sortsByMatchScoreDescending() {
        InfluencerProfile a = profile(1L, "A", "x", "y");
        InfluencerProfile b = profile(2L, "B", "x", "y");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(a, b));
        when(groqApiClient.isConfigured()).thenReturn(true);
        String json = "{\"recommendations\": ["
                + "{\"influencerId\": 1, \"matchScore\": 70, \"reason\": \"low\"},"
                + "{\"influencerId\": 2, \"matchScore\": 99, \"reason\": \"high\"}"
                + "]}";
        when(groqApiClient.getChatCompletion(anyString())).thenReturn(json);

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result).as("result size").hasSize(2),
                () -> assertThat(result.get(0).getMatchScore()).as("first score").isEqualTo(99),
                () -> assertThat(result.get(1).getMatchScore()).as("second score").isEqualTo(70)
        );
    }

    @Test
    void getRecommendations_groqSuccess_enrichesFromProfileWhenPresent() {
        InfluencerProfile inf = profile(10L, "Enriched Name", "NicheX", "NYC");
        inf.setProfilePictureUrl("https://pic.test/id.png");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getChatCompletion(anyString())).thenReturn(
                "{\"recommendations\": [{\"influencerId\": 10, \"matchScore\": 80, \"reason\": \"ok\"}]}");
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(inf));

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result.get(0).getName()).as("enriched name").isEqualTo("Enriched Name"),
                () -> assertThat(result.get(0).getNiche()).as("enriched niche").isEqualTo("NicheX"),
                () -> assertThat(result.get(0).getProfilePictureUrl()).as("profile picture url").isEqualTo("https://pic.test/id.png")
        );
    }

    @Test
    void getRecommendations_groqSuccess_skipsEnrichmentWhenProfileMissing() {
        InfluencerProfile inf = profile(10L, "Alex", "Lifestyle", "NYC");
        when(influencerProfileRepository.findAll()).thenReturn(List.of(inf));
        when(groqApiClient.isConfigured()).thenReturn(true);
        when(groqApiClient.getChatCompletion(anyString())).thenReturn(
                "{\"recommendations\": [{\"influencerId\": 10, \"matchScore\": 80, \"reason\": \"ok\"}]}");
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        List<InfluencerRecommendationDTO> result = aiRecommendationService.getRecommendations(1L);

        assertAll(
                () -> assertThat(result.get(0).getName()).as("name is null").isNull(),
                () -> assertThat(result.get(0).getNiche()).as("niche is null").isNull()
        );
    }

    private static InfluencerProfile profile(long userId, String name, String niche, String location) {
        InfluencerProfile p = new InfluencerProfile();
        p.setUserId(userId);
        p.setName(name);
        p.setNiche(niche);
        p.setLocation(location);
        p.setRate(java.math.BigDecimal.valueOf(100));
        return p;
    }
}
