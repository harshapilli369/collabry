package com.group4.backend.service;
import com.group4.backend.service.profile.BrandProfileService;

import com.group4.backend.dto.BrandProfileRequest;
import com.group4.backend.dto.BrandProfileResponse;
import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandProfileServiceTest {

    @Mock
    private BrandProfileRepository brandProfileRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BrandProfileService brandProfileService;

    private User brandUser;
    private BrandProfile existingProfile;

    @BeforeEach
    void setUp() {
        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);

        existingProfile = new BrandProfile();
        existingProfile.setId(1L);
        existingProfile.setUserId(10L);
        existingProfile.setName("Old Name");
        existingProfile.setIndustry("Tech");
        existingProfile.setWebsite("https://old.com");
        existingProfile.setEmail("old@brand.com");
        existingProfile.setCreatedAt(Instant.now());
        existingProfile.setUpdatedAt(Instant.now());
    }

    @Test
    void getByUserId_whenProfileExists_returnsResponse() {
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.of(existingProfile));

        Optional<BrandProfileResponse> result = brandProfileService.getByUserId(10L);

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getId()).isEqualTo(1L),
                () -> assertThat(result.get().getUserId()).isEqualTo(10L),
                () -> assertThat(result.get().getName()).isEqualTo("Old Name"),
                () -> assertThat(result.get().getIndustry()).isEqualTo("Tech"),
                () -> assertThat(result.get().getWebsite()).isEqualTo("https://old.com"),
                () -> assertThat(result.get().getEmail()).isEqualTo("old@brand.com")
        );
    }

    @Test
    void getByUserId_whenProfileMissing_returnsEmpty() {
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        Optional<BrandProfileResponse> result = brandProfileService.getByUserId(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void createOrUpdateForUser_whenUserNotFound_throws() {
        BrandProfileRequest request = validRequest();
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandProfileService.createOrUpdateForUser(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createOrUpdateForUser_whenUserNotBrand_throws() {
        User influencer = new User("inf@test.com", "pass", Role.INFLUENCER);
        influencer.setId(10L);
        BrandProfileRequest request = validRequest();
        when(userRepository.findById(10L)).thenReturn(Optional.of(influencer));

        assertThatThrownBy(() -> brandProfileService.createOrUpdateForUser(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only brand users");
    }

    @Test
    void createOrUpdateForUser_createsNewProfile() {
        BrandProfileRequest request = validRequest();
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> {
            BrandProfile p = inv.getArgument(0);
            p.setId(1L);
            p.setCreatedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
            return p;
        });

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getUserId()).isEqualTo(10L),
                () -> assertThat(response.getName()).isEqualTo("Acme Inc"),
                () -> assertThat(response.getIndustry()).isEqualTo("Fashion"),
                () -> assertThat(response.getWebsite()).isEqualTo("https://acme.com"),
                () -> assertThat(response.getEmail()).isEqualTo("contact@acme.com"),
                () -> assertThat(response.getBudgetRange()).isEqualTo(BudgetRange.ONE_K_5K)
        );
        verify(brandProfileRepository).save(any(BrandProfile.class));
    }

    @Test
    void createOrUpdateForUser_updatesExistingProfile() {
        BrandProfileRequest request = validRequest();
        request.setName("Acme Updated");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.of(existingProfile));
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getName()).isEqualTo("Acme Updated"),
                () -> assertThat(response.getIndustry()).isEqualTo("Fashion")
        );
        verify(brandProfileRepository).save(any(BrandProfile.class));
    }

    @Test
    void createOrUpdateForUser_normalizesWebsiteWithHttps() {
        BrandProfileRequest request = validRequest();
        request.setWebsite("acme.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertThat(response.getWebsite()).isEqualTo("https://acme.com");
    }

    @Test
    void getPublicProfile_whenProfileExists_returnsResponse() {
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.of(existingProfile));

        Optional<BrandProfileResponse> result = brandProfileService.getPublicProfile(10L);

        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getId()).isEqualTo(1L),
                () -> assertThat(result.get().getUserId()).isEqualTo(10L),
                () -> assertThat(result.get().getName()).isEqualTo("Old Name")
        );
    }

    @Test
    void getPublicProfile_whenProfileMissing_returnsEmpty() {
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        Optional<BrandProfileResponse> result = brandProfileService.getPublicProfile(10L);

        assertThat(result).isEmpty();
    }

    private static BrandProfileRequest validRequest() {
        BrandProfileRequest r = new BrandProfileRequest();
        r.setName("Acme Inc");
        r.setIndustry("Fashion");
        r.setWebsite("https://acme.com");
        r.setEmail("contact@acme.com");
        r.setBudgetRange(BudgetRange.ONE_K_5K);
        return r;
    }
}
