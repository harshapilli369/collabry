package com.group4.backend.service;
import com.group4.backend.service.campaign.CampaignService;

import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.Campaign;
import com.group4.backend.model.CampaignGoal;
import com.group4.backend.model.CampaignStatus;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CampaignService campaignService;

    private User brandUser;
    private CampaignRequest baseRequest;

    @BeforeEach
    void setUp() {
        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);

        baseRequest = new CampaignRequest();
        baseRequest.setName("  Summer Promotion  ");
        baseRequest.setDescription("  seasonal campaign  ");
        baseRequest.setBudgetRange(BudgetRange.FIVE_K_10K);
        baseRequest.setCampaignGoal(CampaignGoal.AWARENESS);
        baseRequest.setPreferredContentTypes("  REELS,STORIES  ");
        baseRequest.setStartDate(LocalDate.now().plusDays(1));
        baseRequest.setEndDate(LocalDate.now().plusDays(30));
        baseRequest.setNumberOfInfluencers(3);
    }

    @Test
    void create_shouldCreateCampaignForBrandUser() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign c = invocation.getArgument(0);
            c.setId(101L);
            return c;
        });

        CampaignResponse response = campaignService.create(10L, baseRequest);

        assertAll(
                () -> assertThat(response.getId()).as("campaign id").isEqualTo(101L),
                () -> assertThat(response.getUserId()).as("user id").isEqualTo(10L),
                () -> assertThat(response.getName()).as("trimmed name").isEqualTo("Summer Promotion"),
                () -> assertThat(response.getDescription()).as("trimmed description").isEqualTo("seasonal campaign"),
                () -> assertThat(response.getPreferredContentTypes()).as("trimmed content types").isEqualTo("REELS,STORIES"),
                () -> assertThat(response.getStatus()).as("status").isEqualTo(CampaignStatus.DRAFT)
        );
    }

    @Test
    void create_shouldNormalizeBlankOptionalFieldsToNull() {
        baseRequest.setDescription("   ");
        baseRequest.setPreferredContentTypes("   ");

        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.create(10L, baseRequest);

        assertAll(
                () -> assertThat(response.getDescription()).as("blank description normalized to null").isNull(),
                () -> assertThat(response.getPreferredContentTypes()).as("blank content types normalized to null").isNull()
        );
    }

    @Test
    void create_shouldAllowNullOptionalFields() {
        baseRequest.setDescription(null);
        baseRequest.setPreferredContentTypes(null);

        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignResponse response = campaignService.create(10L, baseRequest);

        assertAll(
                () -> assertThat(response.getDescription()).as("null description stays null").isNull(),
                () -> assertThat(response.getPreferredContentTypes()).as("null content types stays null").isNull()
        );
    }

    @Test
    void create_shouldThrowWhenUserMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.create(10L, baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void create_shouldThrowWhenUserIsNotBrand() {
        User influencer = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencer.setId(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencer));

        assertThatThrownBy(() -> campaignService.create(20L, baseRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only brand users can create campaigns");
        verify(campaignRepository, never()).save(any(Campaign.class));
    }

    @Test
    void findByUserId_shouldReturnMappedResponses() {
        Campaign campaign = buildCampaign(300L, 10L, "Campaign A");
        when(campaignRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(campaign));

        List<CampaignResponse> responses = campaignService.findByUserId(10L);

        assertAll(
                () -> assertThat(responses).as("result size").hasSize(1),
                () -> assertThat(responses.get(0).getId()).as("campaign id").isEqualTo(300L),
                () -> assertThat(responses.get(0).getName()).as("name").isEqualTo("Campaign A"),
                () -> assertThat(responses.get(0).getBudgetRange()).as("budget range").isEqualTo(BudgetRange.FIVE_K_10K)
        );
    }

    @Test
    void findById_shouldReturnResponseWhenFound() {
        Campaign campaign = buildCampaign(301L, 10L, "Campaign B");
        when(campaignRepository.findById(301L)).thenReturn(Optional.of(campaign));

        Optional<CampaignResponse> response = campaignService.findById(301L);

        assertAll(
                () -> assertThat(response).as("result present").isPresent(),
                () -> assertThat(response.get().getId()).as("campaign id").isEqualTo(301L),
                () -> assertThat(response.get().getStatus()).as("status").isEqualTo(CampaignStatus.DRAFT)
        );
    }

    @Test
    void findById_shouldReturnEmptyWhenMissing() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CampaignResponse> response = campaignService.findById(999L);

        assertThat(response).isEmpty();
    }

    private Campaign buildCampaign(Long id, Long userId, String name) {
        Campaign campaign = new Campaign();
        campaign.setId(id);
        campaign.setUserId(userId);
        campaign.setName(name);
        campaign.setDescription("desc");
        campaign.setBudgetRange(BudgetRange.FIVE_K_10K);
        campaign.setStatus(CampaignStatus.DRAFT);
        campaign.setCampaignGoal(CampaignGoal.AWARENESS);
        campaign.setPreferredContentTypes("REELS");
        campaign.setStartDate(LocalDate.now().plusDays(1));
        campaign.setEndDate(LocalDate.now().plusDays(20));
        campaign.setNumberOfInfluencers(2);
        return campaign;
    }
}
