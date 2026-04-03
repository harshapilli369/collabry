package com.group4.backend.service;
import com.group4.backend.service.profile.InfluencerProfileService;
import com.group4.backend.service.profile.RatingService;

import com.group4.backend.dto.profile.InfluencerProfileRequest;
import com.group4.backend.dto.profile.InfluencerProfileResponse;
import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.dto.rating.RatingResponse;
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

        List<InfluencerProfileResponse> result = influencerProfileService.search(new InfluencerSearchFilter(null, null, null, null, null, null));

        assertAll(
                () -> assertThat(result).as("result size").hasSize(1),
                () -> assertThat(result.get(0).getName()).as("name").isEqualTo("Jane"),
                () -> assertThat(result.get(0).getNiche()).as("niche").isEqualTo("Fashion"),
                () -> assertThat(result.get(0).getLocation()).as("location").isEqualTo("New York"),
                () -> assertThat(result.get(0).getFollowerCount()).as("follower count").isEqualTo(50000L),
                () -> assertThat(result.get(0).getEngagementRate()).as("engagement rate").isEqualByComparingTo("3.5"),
                () -> assertThat(result.get(0).isComplete()).as("complete flag").isTrue()
        );
    }

    @Test
    void search_withFilters_callsRepositoryWithSpecification() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter("Fashion", "NYC", 1000L, 100000L, BigDecimal.valueOf(2.5), null));

        assertThat(result).as("result size").hasSize(1);
        ArgumentCaptor<Specification<InfluencerProfile>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(influencerProfileRepository).findAll(specCaptor.capture());
        assertThat(specCaptor.getValue()).as("specification not null").isNotNull();
    }

    @Test
    void search_emptyResult_returnsEmptyList() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        List<InfluencerProfileResponse> result = influencerProfileService.search(new InfluencerSearchFilter("Tech", null, null, null, null, null));

        assertThat(result).isEmpty();
    }

    @Test
    void search_whenMinFollowersGreaterThanMaxFollowers_throws() {
        assertThatThrownBy(() -> influencerProfileService.search(new InfluencerSearchFilter(null, null, 10000L, 1000L, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minFollowers cannot be greater than maxFollowers");
    }

    @Test
    void search_withPartialNiche_returnsMatchingProfiles() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(new InfluencerSearchFilter("Fash", null, null, null, null, null));

        assertThat(result).as("result size").hasSize(1);
        assertThat(result.get(0).getNiche()).as("niche").isEqualTo("Fashion");
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

        List<InfluencerProfileResponse> result = influencerProfileService.search(new InfluencerSearchFilter("gaming", null, null, null, null, null));

        assertAll(
                () -> assertThat(result).as("result size").hasSize(2),
                () -> assertThat(result.get(0).getNiche()).as("exact match first").isEqualTo("Gaming"),
                () -> assertThat(result.get(1).getNiche()).as("substring match second").isEqualTo("Retro Gaming")
        );
    }

    @Test
    void getByUserId_whenProfileExists_returnsMappedResponse() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(completeProfile));

        Optional<InfluencerProfileResponse> opt = influencerProfileService.getByUserId(20L);

        assertAll(
                () -> assertThat(opt).as("result present").isPresent(),
                () -> assertThat(opt.get().getUserId()).as("user id").isEqualTo(20L),
                () -> assertThat(opt.get().getName()).as("name").isEqualTo("Jane"),
                () -> assertThat(opt.get().getFollowerCount()).as("follower count").isEqualTo(50000L)
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
                () -> assertThat(result).as("result present").isPresent(),
                () -> assertThat(result.get().getAverageRating()).as("average rating").isEqualTo(4.5),
                () -> assertThat(result.get().getTotalRatings()).as("total ratings").isEqualTo(2),
                () -> assertThat(result.get().getRecentReviews()).as("recent reviews size").hasSize(2),
                () -> assertThat(result.get().getRecentReviews().get(0).getRating()).as("first review rating").isEqualTo(5),
                () -> assertThat(result.get().getRecentReviews().get(0).getReview()).as("first review text").isEqualTo("Great!")
        );
    }

    @Test
    void search_withAvailableOnlyTrue_stillInvokesRepository() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(new InfluencerSearchFilter(null, null, null, null, null, true));

        assertThat(result).as("result size").hasSize(1);
        assertThat(result.get(0).isOpenToCollaborations()).as("open to collaborations").isTrue();
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
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.isComplete()).as("draft is not complete").isFalse()
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
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.isComplete()).as("complete flag").isTrue(),
                () -> assertThat(response.getInstagramHandle()).as("instagram handle").isEqualTo("jane_doe"),
                () -> assertThat(response.getRate()).as("rate").isEqualByComparingTo(BigDecimal.valueOf(500))
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

        assertAll(
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.isComplete()).as("complete flag").isTrue()
        );
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

        assertAll(
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.getName()).as("updated name").isEqualTo("Jane Updated")
        );
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
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.isComplete()).as("complete flag").isTrue(),
                () -> assertThat(response.getRate()).as("zero rate").isEqualByComparingTo(BigDecimal.ZERO)
        );
    }

    @Test
    void createOrUpdateForUser_emptyStringFields_shouldBeStoredAsNull() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(true);
        request.setBio("");
        request.setProfilePictureUrl("   ");
        request.setAudienceInfo("");

        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response.getBio()).as("bio null for empty string").isNull(),
                () -> assertThat(response.getProfilePictureUrl()).as("picture url null for blank").isNull(),
                () -> assertThat(response.getAudienceInfo()).as("audience info null for empty").isNull()
        );
    }

    @Test
    void createOrUpdateForUser_withAllSocialHandles_setsAll() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setInstagramHandle("insta");
        request.setYoutubeHandle("yt");
        request.setTiktokHandle("tt");

        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response.getInstagramHandle()).as("instagram handle").isEqualTo("insta"),
                () -> assertThat(response.getYoutubeHandle()).as("youtube handle").isEqualTo("yt"),
                () -> assertThat(response.getTiktokHandle()).as("tiktok handle").isEqualTo("tt"),
                () -> assertThat(response.isComplete()).as("complete flag").isTrue()
        );
    }

    @Test
    void createOrUpdateForUser_withOnlyTiktokHandle_succeeds() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setInstagramHandle(null);
        request.setYoutubeHandle(null);
        request.setTiktokHandle("tiktokuser");

        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertThat(response.isComplete()).isTrue();
    }

    @Test
    void search_withAvailableOnlyFalse_shouldNotFilterByAvailability() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, null, null, null, false));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withOnlyMinFollowers_returnsFilteredResults() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, 1000L, null, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withOnlyMaxFollowers_returnsFilteredResults() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, null, 100000L, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withEngagementRateFilter_returnsResults() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, null, null, BigDecimal.valueOf(1.0), null));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withLocationFilter_returnsResults() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, "New York", null, null, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void updateCollaborationAvailability_whenUserNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.updateCollaborationAvailability(999L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createOrUpdateForUser_withBlankNameLocationNiche_doesNotOverwriteExisting() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(true);
        request.setName("");
        request.setLocation("   ");
        request.setNiche("");

        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(existingProfile));
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response.getName()).as("name preserved").isEqualTo("Jane Doe"),
                () -> assertThat(response.getLocation()).as("location preserved").isEqualTo("NYC"),
                () -> assertThat(response.getNiche()).as("niche preserved").isEqualTo("Fashion")
        );
    }

    @Test
    void createOrUpdateForUser_withNullNameLocationNiche_doesNotOverwriteExisting() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(true);
        request.setName(null);
        request.setLocation(null);
        request.setNiche(null);

        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(existingProfile));
        when(influencerProfileRepository.save(any(InfluencerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(20L, request);

        assertAll(
                () -> assertThat(response.getName()).as("name preserved").isEqualTo("Jane Doe"),
                () -> assertThat(response.getLocation()).as("location preserved").isEqualTo("NYC"),
                () -> assertThat(response.getNiche()).as("niche preserved").isEqualTo("Fashion")
        );
    }

    @Test
    void search_withBlankNiche_treatsAsNoFilter() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter("   ", null, null, null, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withBlankLocation_treatsAsNoFilter() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, "   ", null, null, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void search_withEqualMinAndMaxFollowers_succeeds() {
        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, 50000L, 50000L, null, null));

        assertThat(result).hasSize(1);
    }

    @Test
    void createOrUpdateForUser_completeWithBlankOnlyHandles_throws() {
        InfluencerProfileRequest request = completeRequest();
        request.setSaveAsDraft(false);
        request.setInstagramHandle("   ");
        request.setYoutubeHandle("   ");
        request.setTiktokHandle("   ");
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> influencerProfileService.createOrUpdateForUser(20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one social media handle");
    }

    @Test
    void search_multipleProfilesWithNullCreatedAt_doesNotThrow() {
        InfluencerProfile noDate = new InfluencerProfile();
        noDate.setId(99L);
        noDate.setUserId(99L);
        noDate.setName("No Date");
        noDate.setNiche("Tech");
        noDate.setComplete(true);
        noDate.setCreatedAt(null);

        when(influencerProfileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(completeProfile, noDate));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                new InfluencerSearchFilter(null, null, null, null, null, null));

        assertThat(result).hasSize(2);
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
