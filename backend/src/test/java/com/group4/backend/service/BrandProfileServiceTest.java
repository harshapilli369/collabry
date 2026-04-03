package com.group4.backend.service;
import com.group4.backend.service.profile.BrandProfileService;

import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.dto.profile.BrandProfileResponse;
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
                () -> assertThat(result).as("result present").isPresent(),
                () -> assertThat(result.get().getId()).as("profile id").isEqualTo(1L),
                () -> assertThat(result.get().getUserId()).as("user id").isEqualTo(10L),
                () -> assertThat(result.get().getName()).as("name").isEqualTo("Old Name"),
                () -> assertThat(result.get().getIndustry()).as("industry").isEqualTo("Tech"),
                () -> assertThat(result.get().getWebsite()).as("website").isEqualTo("https://old.com"),
                () -> assertThat(result.get().getEmail()).as("email").isEqualTo("old@brand.com")
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
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.getUserId()).as("user id").isEqualTo(10L),
                () -> assertThat(response.getName()).as("name").isEqualTo("Acme Inc"),
                () -> assertThat(response.getIndustry()).as("industry").isEqualTo("Fashion"),
                () -> assertThat(response.getWebsite()).as("website").isEqualTo("https://acme.com"),
                () -> assertThat(response.getEmail()).as("email").isEqualTo("contact@acme.com"),
                () -> assertThat(response.getBudgetRange()).as("budget range").isEqualTo(BudgetRange.ONE_K_5K)
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
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.getName()).as("updated name").isEqualTo("Acme Updated"),
                () -> assertThat(response.getIndustry()).as("industry").isEqualTo("Fashion")
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
                () -> assertThat(result).as("result present").isPresent(),
                () -> assertThat(result.get().getId()).as("profile id").isEqualTo(1L),
                () -> assertThat(result.get().getUserId()).as("user id").isEqualTo(10L),
                () -> assertThat(result.get().getName()).as("name").isEqualTo("Old Name")
        );
    }

    @Test
    void getPublicProfile_whenProfileMissing_returnsEmpty() {
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        Optional<BrandProfileResponse> result = brandProfileService.getPublicProfile(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void createOrUpdateForUser_websiteWithHttpPrefix_keepsAsIs() {
        BrandProfileRequest request = validRequest();
        request.setWebsite("http://acme.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertThat(response.getWebsite()).isEqualTo("http://acme.com");
    }

    @Test
    void createOrUpdateForUser_websiteWithFtpPrefix_keepsAsIs() {
        BrandProfileRequest request = validRequest();
        request.setWebsite("ftp://files.acme.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertThat(response.getWebsite()).isEqualTo("ftp://files.acme.com");
    }

    @Test
    void createOrUpdateForUser_websiteNull_storesNull() {
        BrandProfileRequest request = validRequest();
        request.setWebsite(null);
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertThat(response.getWebsite()).isNull();
    }

    @Test
    void createOrUpdateForUser_websiteEmpty_storesEmpty() {
        BrandProfileRequest request = validRequest();
        request.setWebsite("   ");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertThat(response.getWebsite()).isEmpty();
    }

    @Test
    void createOrUpdateForUser_emptyOptionalFields_storedAsNull() {
        BrandProfileRequest request = validRequest();
        request.setLogoUrl("   ");
        request.setDescription("");
        request.setInstagramUrl("   ");
        request.setLinkedInUrl("");
        request.setTwitterUrl("   ");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertAll(
                () -> assertThat(response.getLogoUrl()).as("logo url null").isNull(),
                () -> assertThat(response.getDescription()).as("description null").isNull(),
                () -> assertThat(response.getInstagramUrl()).as("instagram url null").isNull(),
                () -> assertThat(response.getLinkedInUrl()).as("linkedin url null").isNull(),
                () -> assertThat(response.getTwitterUrl()).as("twitter url null").isNull()
        );
    }

    @Test
    void createOrUpdateForUser_nullOptionalFields_storedAsNull() {
        BrandProfileRequest request = validRequest();
        request.setLogoUrl(null);
        request.setDescription(null);
        request.setInstagramUrl(null);
        request.setLinkedInUrl(null);
        request.setTwitterUrl(null);
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertAll(
                () -> assertThat(response.getLogoUrl()).as("logo url null").isNull(),
                () -> assertThat(response.getDescription()).as("description null").isNull(),
                () -> assertThat(response.getInstagramUrl()).as("instagram url null").isNull(),
                () -> assertThat(response.getLinkedInUrl()).as("linkedin url null").isNull(),
                () -> assertThat(response.getTwitterUrl()).as("twitter url null").isNull()
        );
    }

    @Test
    void createOrUpdateForUser_withAllOptionalFields_storesAll() {
        BrandProfileRequest request = validRequest();
        request.setLogoUrl("https://logo.test/brand.png");
        request.setDescription("A great brand");
        request.setInstagramUrl("https://instagram.com/brand");
        request.setLinkedInUrl("https://linkedin.com/company/brand");
        request.setTwitterUrl("https://twitter.com/brand");
        when(userRepository.findById(10L)).thenReturn(Optional.of(brandUser));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(brandProfileRepository.save(any(BrandProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(10L, request);

        assertAll(
                () -> assertThat(response.getLogoUrl()).isEqualTo("https://logo.test/brand.png"),
                () -> assertThat(response.getDescription()).isEqualTo("A great brand"),
                () -> assertThat(response.getInstagramUrl()).isEqualTo("https://instagram.com/brand"),
                () -> assertThat(response.getLinkedInUrl()).isEqualTo("https://linkedin.com/company/brand"),
                () -> assertThat(response.getTwitterUrl()).isEqualTo("https://twitter.com/brand")
        );
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
