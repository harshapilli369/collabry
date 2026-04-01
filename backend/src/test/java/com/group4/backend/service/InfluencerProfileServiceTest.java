package com.group4.backend.service;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.dto.RatingResponse;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Merged: brand search + filters (develop) and profile CRUD + ratings (feature).
 */
@ExtendWith(MockitoExtension.class)
class InfluencerProfileServiceTest {

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RatingService ratingService;

    @InjectMocks
    private InfluencerProfileService influencerProfileService;

    private User influencerUser;
    private InfluencerProfile existingProfile;
    private InfluencerProfile completeProfile;

    @BeforeEach
    void setUp() {
        lenient().when(ratingService.getRatingSummary(anyLong(), anyInt()))
                .thenReturn(new RatingService.RatingSummary(0.0, 0, List.of()));

        influencerUser = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencerUser.setId(20L);

        existingProfile = new InfluencerProfile();
        existingProfile.setId(2L);
        existingProfile.setUserId(20L);
        existingProfile.setName("Jane Doe");
        existingProfile.setAge(25);
        existingProfile.setLocation("NYC");
        existingProfile.setNiche("Fashion");
        existingProfile.setComplete(false);
        existingProfile.setCreatedAt(Instant.now());
        existingProfile.setUpdatedAt(Instant.now());

        completeProfile = new InfluencerProfile();
        completeProfile.setId(1L);
        completeProfile.setUserId(20L);
        completeProfile.setName("Jane");
        completeProfile.setNiche("Fashion");
        completeProfile.setLocation("New York");
        completeProfile.setComplete(true);
        completeProfile.setFollowerCount(50000L);
        completeProfile.setEngagementRate(BigDecimal.valueOf(3.5));
        completeProfile.setRate(BigDecimal.valueOf(500));
        completeProfile.setOpenToCollaborations(true);
    }

    @Test
    void search_withNoFilters_returnsOnlyCompleteProfilesFromRepository() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(null, null, null, null, null, null);

        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).getName()).isEqualTo("Jane"),
                () -> assertThat(result.get(0).getNiche()).isEqualTo("Fashion"),
                () -> assertThat(result.get(0).getLocation()).isEqualTo("New York"),
                () -> assertThat(result.get(0).getFollowerCount()).isEqualTo(50000L),
                () -> assertThat(result.get(0).getEngagementRate()).isEqualByComparingTo("3.5"),
                () -> assertThat(result.get(0).isComplete()).isTrue()
        );
    }

    @Test
    void search_withFilters_callsRepositoryWithSpecification() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                "Fashion", "NYC", 1000L, 100000L, BigDecimal.valueOf(2.5), null);

        assertThat(result).hasSize(1);
        ArgumentCaptor<Specification<InfluencerProfile>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(influencerProfileRepository).findAll(specCaptor.capture());
        assertThat(specCaptor.getValue()).isNotNull();
    }

    @Test
    void search_emptyResult_returnsEmptyList() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        List<InfluencerProfileResponse> result = influencerProfileService.search("Tech", null, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void search_whenMinFollowersGreaterThanMaxFollowers_throws() {
        assertThatThrownBy(() -> influencerProfileService.search(null, null, 10000L, 1000L, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minFollowers cannot be greater than maxFollowers");
    }

    @Test
    void search_withPartialNiche_returnsMatchingProfiles() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search("Fash", null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNiche()).isEqualTo("Fashion");
    }

    @Test
    void search_withNicheQuery_ordersExactMatchBeforeSubstringMatch() {
        InfluencerProfile retroGaming = new InfluencerProfile();
        retroGaming.setId(10L);
        retroGaming.setUserId(101L);
        retroGaming.setName("Retro");
        retroGaming.setNiche("Retro Gaming");
        retroGaming.setLocation("Austin");
        retroGaming.setComplete(true);
        retroGaming.setFollowerCount(10_000L);
        retroGaming.setEngagementRate(BigDecimal.valueOf(3.0));
        retroGaming.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        InfluencerProfile pureGaming = new InfluencerProfile();
        pureGaming.setId(11L);
        pureGaming.setUserId(102L);
        pureGaming.setName("Pro");
        pureGaming.setNiche("Gaming");
        pureGaming.setLocation("Austin");
        pureGaming.setComplete(true);
        pureGaming.setFollowerCount(10_000L);
        pureGaming.setEngagementRate(BigDecimal.valueOf(3.0));
        pureGaming.setCreatedAt(Instant.parse("2020-01-01T00:00:00Z"));

        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(retroGaming, pureGaming));

        List<InfluencerProfileResponse> result = influencerProfileService.search("gaming", null, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNiche()).isEqualTo("Gaming");
        assertThat(result.get(1).getNiche()).isEqualTo("Retro Gaming");
    }

    @Test
    void getByUserId_whenProfileExists_returnsMappedResponse() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(completeProfile));

        Optional<InfluencerProfileResponse> opt = influencerProfileService.getByUserId(20L);

        assertAll(
                () -> assertThat(opt).isPresent(),
                () -> assertThat(opt.get().getUserId()).isEqualTo(20L),
                () -> assertThat(opt.get().getName()).isEqualTo("Jane"),
                () -> assertThat(opt.get().getFollowerCount()).isEqualTo(50000L)
        );
    }

    @Test
    void getByUserId_whenProfileMissing_returnsEmpty() {
        when(influencerProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<InfluencerProfileResponse> opt = influencerProfileService.getByUserId(999L);

        assertThat(opt).isEmpty();
    }

    @Test
    void getByUserId_whenProfileExists_includesRatingDataOnProfile() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(existingProfile));
        RatingResponse r1 = new RatingResponse();
        r1.setRating(5);
        r1.setReview("Great!");
        RatingResponse r2 = new RatingResponse();
        r2.setRating(4);
        r2.setReview("Good collaboration");
        List<RatingResponse> reviews = List.of(r1, r2);
        when(ratingService.getRatingSummary(20L, 5))
                .thenReturn(new RatingService.RatingSummary(4.5, 2, reviews));

        Optional<InfluencerProfileResponse> result = influencerProfileService.getByUserId(20L);

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getAverageRating()).isEqualTo(4.5),
                () -> assertThat(result.get().getTotalRatings()).isEqualTo(2),
                () -> assertThat(result.get().getRecentReviews()).hasSize(2),
                () -> assertThat(result.get().getRecentReviews().get(0).getRating()).isEqualTo(5),
                () -> assertThat(result.get().getRecentReviews().get(0).getReview()).isEqualTo("Great!")
        );
    }

    @Test
    void search_withAvailableOnlyTrue_stillInvokesRepository() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(null, null, null, null, null, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isOpenToCollaborations()).isTrue();
        verify(influencerProfileRepository).findAll(any(Specification.class));
    }

    @Test
    void updateCollaborationAvailability_whenProfileExists_updatesAndReturns() {
        completeProfile.setOpenToCollaborations(true);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(completeProfile));
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.updateCollaborationAvailability(20L, false);

        assertThat(response.isOpenToCollaborations()).isFalse();
        verify(influencerProfileRepository).save(any(InfluencerProfile.class));
    }

    @Test
    void updateCollaborationAvailability_whenProfileMissing_throws() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.updateCollaborationAvailability(20L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("profile not found");
    }

    @Test
    void updateCollaborationAvailability_whenUserNotInfluencer_throws() {
        User brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(brandUser));

        assertThatThrownBy(() -> influencerProfileService.updateCollaborationAvailability(20L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only influencer users");
    }

    @Test
    void createOrUpdateForUser_whenUserNotFound_throws() {
        InfluencerProfileRequest request = completeRequest();
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createOrUpdateForUser_whenUserNotInfluencer_throws() {
        User brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(20L);
        InfluencerProfileRequest request = completeRequest();
        when(userRepository.findById(20L)).thenReturn(Optional.of(brandUser));

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only influencer users");
    }

    @Test
    void createOrUpdateForUser_saveAsDraft_setsCompleteFalse() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(true);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.isComplete()).isFalse()
        );
        verify(influencerProfileRepository).save(any(InfluencerProfile.class));
    }

    @Test
    void createOrUpdateForUser_completeWithoutSocialHandle_throws() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setInstagramHandle(null);
        request.setYoutubeHandle(null);
        request.setTiktokHandle(null);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one social media handle");
    }

    @Test
    void createOrUpdateForUser_completeWithNegativeRate_throws() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setRate(BigDecimal.valueOf(-100));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rate is required");
    }

    @Test
    void createOrUpdateForUser_completeWithNullRate_throws() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setRate(null);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rate is required");
    }

    @Test
    void createOrUpdateForUser_completeWithHandleAndRate_setsCompleteTrue() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.isComplete()).isTrue(),
                () -> assertThat(response.getInstagramHandle()).isEqualTo("jane_doe"),
                () -> assertThat(response.getRate()).isEqualByComparingTo(BigDecimal.valueOf(500))
        );
        verify(influencerProfileRepository).save(any(InfluencerProfile.class));
    }

    @Test
    void createOrUpdateForUser_completeWithOnlyYoutubeHandle_succeeds() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setInstagramHandle(null);
        request.setTiktokHandle(null);
        request.setYoutubeHandle("janedoe");
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertThat(response).isNotNull();
        assertThat(response.isComplete()).isTrue();
    }

    @Test
    void createOrUpdateForUser_updatesExistingProfile() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(true);
        request.setName("Jane Updated");
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(existingProfile));
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane Updated");
        verify(influencerProfileRepository).save(any(InfluencerProfile.class));
    }

    @Test
    void createOrUpdateForUser_completeWithZeroRate_succeeds() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setRate(BigDecimal.ZERO);
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.isComplete()).isTrue(),
                () -> assertThat(response.getRate()).isEqualByComparingTo(BigDecimal.ZERO)
        );
    }

    private static InfluencerProfileRequest completeRequest() {
        InfluencerProfileRequest r = new InfluencerProfileRequest();
        r.setName("Jane Doe");
        r.setAge(25);
        r.setLocation("NYC");
        r.setNiche("Fashion");
        r.setInstagramHandle("jane_doe");
        r.setRate(BigDecimal.valueOf(500));
        r.setSaveAsDraft(false);
        return r;
    }
}
