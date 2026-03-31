package com.group4.backend.config;

import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @Test
    void createsAdminBrandAndInfluencerWhenMissing() throws Exception {
        when(passwordEncoder.encode(any())).thenReturn("ENC");
        when(userRepository.existsByEmail("admin@collabry.com")).thenReturn(false);
        when(userRepository.existsByEmail("brand@collabry.com")).thenReturn(false);
        when(userRepository.existsByEmail("influencer@collabry.com")).thenReturn(false);

        User savedInfluencer = new User("influencer@collabry.com", "ENC", Role.INFLUENCER);
        savedInfluencer.setId(9L);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getEmail().equals("influencer@collabry.com")) {
                u.setId(9L);
            }
            return u;
        });

        when(influencerProfileRepository.findByUserId(9L)).thenReturn(Optional.empty());

        new DataInitializer(userRepository, passwordEncoder, influencerProfileRepository)
                .initializeData()
                .run();

        verify(userRepository, times(3)).save(any(User.class));
        ArgumentCaptor<InfluencerProfile> profileCap = ArgumentCaptor.forClass(InfluencerProfile.class);
        verify(influencerProfileRepository).save(profileCap.capture());
        assertThat(profileCap.getValue().getUserId()).isEqualTo(9L);
    }

    @Test
    void skipsUserCreationWhenEmailsAlreadyExist() throws Exception {
        when(userRepository.existsByEmail("admin@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("brand@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("influencer@collabry.com")).thenReturn(true);

        User existing = new User("influencer@collabry.com", "ENC", Role.INFLUENCER);
        existing.setId(42L);
        when(userRepository.findByEmail("influencer@collabry.com")).thenReturn(Optional.of(existing));
        when(influencerProfileRepository.findByUserId(42L)).thenReturn(Optional.empty());

        new DataInitializer(userRepository, passwordEncoder, influencerProfileRepository)
                .initializeData()
                .run();

        verify(userRepository, never()).save(any(User.class));
        ArgumentCaptor<InfluencerProfile> profileCap = ArgumentCaptor.forClass(InfluencerProfile.class);
        verify(influencerProfileRepository).save(profileCap.capture());
        assertThat(profileCap.getValue().getUserId()).isEqualTo(42L);
    }

    @Test
    void doesNotTouchProfileWhenInfluencerUserCannotBeResolved() throws Exception {
        when(userRepository.existsByEmail("admin@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("brand@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("influencer@collabry.com")).thenReturn(true);
        when(userRepository.findByEmail("influencer@collabry.com")).thenReturn(Optional.empty());

        new DataInitializer(userRepository, passwordEncoder, influencerProfileRepository)
                .initializeData()
                .run();

        verify(influencerProfileRepository, never()).save(any());
    }

    @Test
    void fillsDefaultsOnlyWhereProfileFieldsAreNull() throws Exception {
        when(userRepository.existsByEmail("admin@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("brand@collabry.com")).thenReturn(true);
        when(userRepository.existsByEmail("influencer@collabry.com")).thenReturn(true);

        User existing = new User("influencer@collabry.com", "ENC", Role.INFLUENCER);
        existing.setId(7L);
        when(userRepository.findByEmail("influencer@collabry.com")).thenReturn(Optional.of(existing));

        InfluencerProfile partial = new InfluencerProfile();
        partial.setUserId(7L);
        partial.setName("Keep Name");
        partial.setAge(30);
        partial.setLocation("Toronto");
        partial.setNiche("Tech");
        partial.setBio("bio");
        partial.setInstagramHandle("@ig");
        partial.setRate(java.math.BigDecimal.TEN);
        partial.setFollowerCount(100L);
        partial.setEngagementRate(java.math.BigDecimal.ONE);
        when(influencerProfileRepository.findByUserId(7L)).thenReturn(Optional.of(partial));

        new DataInitializer(userRepository, passwordEncoder, influencerProfileRepository)
                .initializeData()
                .run();

        ArgumentCaptor<InfluencerProfile> cap = ArgumentCaptor.forClass(InfluencerProfile.class);
        verify(influencerProfileRepository).save(cap.capture());
        InfluencerProfile saved = cap.getValue();
        assertAll(
                () -> assertThat(saved.getName()).isEqualTo("Keep Name"),
                () -> assertThat(saved.getAge()).isEqualTo(30),
                () -> assertThat(saved.isComplete()).isTrue()
        );
    }
}
