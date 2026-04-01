package com.group4.backend.config;

import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.BrandProfileRepository;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @Mock
    private BrandProfileRepository brandProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void stubEncoder() {
        when(passwordEncoder.encode(any())).thenReturn("ENC");
    }

    @Test
    void seedsBrandWhenAdminBrandComMissing() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return !"admin@brand.com".equals(email);
        });

        User savedBrand = new User("admin@brand.com", "ENC", Role.BRAND);
        savedBrand.setId(100L);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if ("admin@brand.com".equals(u.getEmail())) {
                u.setId(100L);
            }
            return u;
        });

        new DatabaseSeeder(userRepository, influencerProfileRepository, brandProfileRepository, passwordEncoder)
                .run();

        ArgumentCaptor<BrandProfile> brandCap = ArgumentCaptor.forClass(BrandProfile.class);
        verify(brandProfileRepository).save(brandCap.capture());
        assertAll(
                () -> assertThat(brandCap.getValue().getUserId()).isEqualTo(100L),
                () -> assertThat(brandCap.getValue().getName()).contains("Tech Haven")
        );
    }

    @Test
    void skipsBrandSeedWhenAlreadyPresent() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        new DatabaseSeeder(userRepository, influencerProfileRepository, brandProfileRepository, passwordEncoder)
                .run();

        verify(brandProfileRepository, never()).save(any());
    }

    @Test
    void seedsInfluencerWhenEmailMissing() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return !"gamer1@test.com".equals(email);
        });

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if ("gamer1@test.com".equals(u.getEmail())) {
                u.setId(200L);
            }
            return u;
        });

        new DatabaseSeeder(userRepository, influencerProfileRepository, brandProfileRepository, passwordEncoder)
                .run();

        ArgumentCaptor<InfluencerProfile> cap = ArgumentCaptor.forClass(InfluencerProfile.class);
        verify(influencerProfileRepository, atLeastOnce()).save(cap.capture());
        assertThat(cap.getAllValues())
                .extracting(InfluencerProfile::getName)
                .contains("Taylor");
    }

    @Test
    void verifiesUnverifiedBrandUsers() throws Exception {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        User brand = new User("b@brand.com", "ENC", Role.BRAND);
        brand.setVerified(false);
        when(userRepository.findAll()).thenReturn(List.of(brand));

        new DatabaseSeeder(userRepository, influencerProfileRepository, brandProfileRepository, passwordEncoder)
                .run();

        assertThat(brand.isVerified()).isTrue();
        verify(userRepository).save(brand);
    }
}
