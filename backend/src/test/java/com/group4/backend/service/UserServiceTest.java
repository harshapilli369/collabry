package com.group4.backend.service;

import com.group4.backend.dto.profile.InfluencerSearchResult;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @Mock
    private BrandProfileRepository brandProfileRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void listInfluencers_returnsEmptyWhenNoInfluencers() {
        when(userRepository.findByRole(Role.INFLUENCER)).thenReturn(List.of());

        List<InfluencerSearchResult> results = userService.listInfluencers();

        assertThat(results).isEmpty();
    }

    @Test
    void listInfluencers_usesProfileNameAsDisplayNameWhenPresent() {
        User u1 = new User("inf1@test.com", "p", Role.INFLUENCER);
        u1.setId(1L);
        InfluencerProfile profile = new InfluencerProfile();
        profile.setName("Creator One");

        when(userRepository.findByRole(Role.INFLUENCER)).thenReturn(List.of(u1));
        when(influencerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        List<InfluencerSearchResult> results = userService.listInfluencers();

        assertAll(
                () -> assertThat(results).as("result size").hasSize(1),
                () -> assertThat(results.get(0).getId()).as("user id").isEqualTo(1L),
                () -> assertThat(results.get(0).getEmail()).as("email").isEqualTo("inf1@test.com"),
                () -> assertThat(results.get(0).getDisplayName()).as("display name from profile").isEqualTo("Creator One")
        );
    }

    @Test
    void listInfluencers_fallsBackToEmailWhenNoProfile() {
        User u1 = new User("inf2@test.com", "p", Role.INFLUENCER);
        u1.setId(2L);

        when(userRepository.findByRole(Role.INFLUENCER)).thenReturn(List.of(u1));
        when(influencerProfileRepository.findByUserId(2L)).thenReturn(Optional.empty());

        List<InfluencerSearchResult> results = userService.listInfluencers();

        assertAll(
                () -> assertThat(results).as("result size").hasSize(1),
                () -> assertThat(results.get(0).getDisplayName()).as("display name fallback to email").isEqualTo("inf2@test.com")
        );
    }

    @Test
    void listInfluencers_mapsMultipleInfluencers() {
        User u1 = new User("a@test.com", "p", Role.INFLUENCER);
        u1.setId(1L);
        User u2 = new User("b@test.com", "p", Role.INFLUENCER);
        u2.setId(2L);
        InfluencerProfile p2 = new InfluencerProfile();
        p2.setName("B Name");

        when(userRepository.findByRole(Role.INFLUENCER)).thenReturn(List.of(u1, u2));
        when(influencerProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(influencerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(p2));

        List<InfluencerSearchResult> results = userService.listInfluencers();

        assertAll(
                () -> assertThat(results).as("result size").hasSize(2),
                () -> assertThat(results.get(0).getDisplayName()).as("first display name").isEqualTo("a@test.com"),
                () -> assertThat(results.get(1).getDisplayName()).as("second display name").isEqualTo("B Name")
        );
    }

    @Test
    void linkSocialAccount_influencer_updatesProfileAndDoesNotAutoVerify() {
        User user = new User("influencer@test.com", "pass", Role.INFLUENCER);
        user.setId(10L);
        user.setVerified(false);

        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(10L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("@creator");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(10L, request);

        assertThat(user.isVerified()).as("verified status unchanged").isFalse();
        assertThat(profile.getInstagramHandle()).as("instagram handle").isEqualTo("@creator");
        verify(influencerProfileRepository).save(profile);
        verify(userRepository, never()).save(user);
    }

    @Test
    void linkSocialAccount_brand_updatesProfileAndDoesNotAutoVerify() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(20L);
        user.setVerified(false);

        BrandProfile profile = new BrandProfile();
        profile.setUserId(20L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("LINKEDIN");
        request.setHandle("https://linkedin.com/company/test");

        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(20L, request);

        assertThat(user.isVerified()).as("verified status unchanged").isFalse();
        assertThat(profile.getLinkedInUrl()).as("linkedin url").isEqualTo("https://linkedin.com/company/test");
        verify(brandProfileRepository).save(profile);
        verify(userRepository, never()).save(user);
    }

    @Test
    void linkSocialAccount_throwsWhenUserNotFound() {
        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("@x");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.linkSocialAccount(99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with ID: 99");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void linkSocialAccount_influencer_youtube_updatesYoutubeHandle() {
        User user = new User("inf@test.com", "pass", Role.INFLUENCER);
        user.setId(10L);
        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(10L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("youtube");
        request.setHandle("@mychannel");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(10L, request);

        assertThat(profile.getYoutubeHandle()).isEqualTo("@mychannel");
        verify(influencerProfileRepository).save(profile);
    }

    @Test
    void linkSocialAccount_influencer_tiktok_updatesTiktokHandle() {
        User user = new User("inf@test.com", "pass", Role.INFLUENCER);
        user.setId(10L);
        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(10L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("TIKTOK");
        request.setHandle("@tiktokuser");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(10L, request);

        assertThat(profile.getTiktokHandle()).isEqualTo("@tiktokuser");
        verify(influencerProfileRepository).save(profile);
    }

    @Test
    void linkSocialAccount_brand_instagram_updatesInstagramUrl() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(20L);
        BrandProfile profile = new BrandProfile();
        profile.setUserId(20L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("https://instagram.com/brand");

        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(20L, request);

        assertThat(profile.getInstagramUrl()).isEqualTo("https://instagram.com/brand");
        verify(brandProfileRepository).save(profile);
    }

    @Test
    void linkSocialAccount_brand_twitter_updatesTwitterUrl() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(20L);
        BrandProfile profile = new BrandProfile();
        profile.setUserId(20L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("TWITTER");
        request.setHandle("https://twitter.com/brand");

        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(20L)).thenReturn(Optional.of(profile));

        userService.linkSocialAccount(20L, request);

        assertThat(profile.getTwitterUrl()).isEqualTo("https://twitter.com/brand");
        verify(brandProfileRepository).save(profile);
    }

    @Test
    void linkSocialAccount_influencer_noProfile_doesNotSave() {
        User user = new User("inf@test.com", "pass", Role.INFLUENCER);
        user.setId(10L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("@handle");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(influencerProfileRepository.findByUserId(10L)).thenReturn(Optional.empty());

        userService.linkSocialAccount(10L, request);

        assertThat(user.isVerified()).as("verified status unchanged").isFalse();
        verify(influencerProfileRepository, never()).save(any());
    }

    @Test
    void linkSocialAccount_brand_noProfile_doesNotSave() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(20L);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("LINKEDIN");
        request.setHandle("https://linkedin.com/test");

        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(20L)).thenReturn(Optional.empty());

        userService.linkSocialAccount(20L, request);

        assertThat(user.isVerified()).as("verified status unchanged").isFalse();
        verify(brandProfileRepository, never()).save(any());
    }
}
