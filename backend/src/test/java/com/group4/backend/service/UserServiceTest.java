package com.group4.backend.service;

import com.group4.backend.dto.InfluencerSearchResult;
import com.group4.backend.dto.SocialLinkRequest;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

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
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).getId()).isEqualTo(1L),
                () -> assertThat(results.get(0).getEmail()).isEqualTo("inf1@test.com"),
                () -> assertThat(results.get(0).getDisplayName()).isEqualTo("Creator One")
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
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).getDisplayName()).isEqualTo("inf2@test.com")
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
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results.get(0).getDisplayName()).isEqualTo("a@test.com"),
                () -> assertThat(results.get(1).getDisplayName()).isEqualTo("B Name")
        );
    }

    @Test
    void linkSocialAccount_setsVerifiedTrueAndSavesUser() {
        User user = new User("user@test.com", "pass", Role.INFLUENCER);
        user.setId(10L);
        user.setVerified(false);

        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("@creator");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        userService.linkSocialAccount(10L, request);

        assertThat(user.isVerified()).isTrue();
        verify(userRepository).save(user);
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
}
