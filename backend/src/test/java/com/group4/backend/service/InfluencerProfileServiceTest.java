package com.group4.backend.service;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfluencerProfileServiceTest {

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InfluencerProfileService influencerProfileService;

    private User influencerUser;
    private InfluencerProfile existingProfile;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void getByUserId_whenProfileExists_returnsResponse() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(existingProfile));

        Optional<InfluencerProfileResponse> result = influencerProfileService.getByUserId(20L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
        assertThat(result.get().getUserId()).isEqualTo(20L);
        assertThat(result.get().getName()).isEqualTo("Jane Doe");
        assertThat(result.get().getAge()).isEqualTo(25);
        assertThat(result.get().getLocation()).isEqualTo("NYC");
        assertThat(result.get().getNiche()).isEqualTo("Fashion");
        assertThat(result.get().isComplete()).isFalse();
    }

    @Test
    void getByUserId_whenProfileMissing_returnsEmpty() {
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        Optional<InfluencerProfileResponse> result = influencerProfileService.getByUserId(20L);

        assertThat(result).isEmpty();
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

        assertThat(response).isNotNull();
        assertThat(response.isComplete()).isFalse();
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

        assertThat(response).isNotNull();
        assertThat(response.isComplete()).isTrue();
        assertThat(response.getInstagramHandle()).isEqualTo("jane_doe");
        assertThat(response.getRate()).isEqualByComparingTo(BigDecimal.valueOf(500));
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

        assertThat(response).isNotNull();
        assertThat(response.isComplete()).isTrue();
        assertThat(response.getRate()).isEqualByComparingTo(BigDecimal.ZERO);
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
