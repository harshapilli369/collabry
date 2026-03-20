package com.group4.backend.service;

import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD-style unit tests for InfluencerProfileService.
 * Covers search (filter params, only complete profiles) and getByUserId.
 */
@ExtendWith(MockitoExtension.class)
class InfluencerProfileServiceTest {

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InfluencerProfileService influencerProfileService;

    private InfluencerProfile completeProfile;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void search_withNoFilters_returnsOnlyCompleteProfilesFromRepository() {
        when(influencerProfileRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "createdAt"))))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Jane");
        assertThat(result.get(0).getNiche()).isEqualTo("Fashion");
        assertThat(result.get(0).getLocation()).isEqualTo("New York");
        assertThat(result.get(0).getFollowerCount()).isEqualTo(50000L);
        assertThat(result.get(0).getEngagementRate()).isEqualByComparingTo("3.5");
        assertThat(result.get(0).isComplete()).isTrue();
    }

    @Test
    void search_withFilters_callsRepositoryWithSpecification() {
        when(influencerProfileRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "createdAt"))))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search(
                "Fashion", "NYC", 1000L, 100000L, BigDecimal.valueOf(2.5));

        assertThat(result).hasSize(1);
        ArgumentCaptor<Specification<InfluencerProfile>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(influencerProfileRepository).findAll(specCaptor.capture(), eq(Sort.by(Sort.Direction.DESC, "createdAt")));
        assertThat(specCaptor.getValue()).isNotNull();
    }

    @Test
    void search_emptyResult_returnsEmptyList() {
        when(influencerProfileRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        List<InfluencerProfileResponse> result = influencerProfileService.search("Tech", null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void search_whenMinFollowersGreaterThanMaxFollowers_throws() {
        assertThatThrownBy(() -> influencerProfileService.search(null, null, 10000L, 1000L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minFollowers cannot be greater than maxFollowers");
    }

    @Test
    void search_withPartialNiche_returnsMatchingProfiles() {
        when(influencerProfileRepository.findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "createdAt"))))
                .thenReturn(List.of(completeProfile));

        List<InfluencerProfileResponse> result = influencerProfileService.search("Fash", null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNiche()).isEqualTo("Fashion");
    }

    @Test
    void getByUserId_whenProfileExists_returnsMappedResponse() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(completeProfile));

        Optional<InfluencerProfileResponse> opt = influencerProfileService.getByUserId(20L);

        assertThat(opt).isPresent();
        assertThat(opt.get().getUserId()).isEqualTo(20L);
        assertThat(opt.get().getName()).isEqualTo("Jane");
        assertThat(opt.get().getFollowerCount()).isEqualTo(50000L);
    }

    @Test
    void getByUserId_whenProfileMissing_returnsEmpty() {
        when(influencerProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        Optional<InfluencerProfileResponse> opt = influencerProfileService.getByUserId(999L);

        assertThat(opt).isEmpty();
    }
}
