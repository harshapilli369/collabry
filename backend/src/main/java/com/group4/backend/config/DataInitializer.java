package com.group4.backend.config;

import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class DataInitializer {

    private static final int SEED_INFLUENCER_AGE = 25;
    private static final long SEED_INFLUENCER_FOLLOWERS = 5000L;
    private static final java.math.BigDecimal SEED_INFLUENCER_RATE = java.math.BigDecimal.valueOf(500);
    private static final java.math.BigDecimal SEED_INFLUENCER_ENGAGEMENT = java.math.BigDecimal.valueOf(4.5);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InfluencerProfileRepository influencerProfileRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          InfluencerProfileRepository influencerProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.influencerProfileRepository = influencerProfileRepository;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            createAdminUser();
            createBrandUser();
            User influencerUser = getOrCreateInfluencerUser();
            if (influencerUser != null) {
                upsertInfluencerProfile(influencerUser);
            }
        };
    }

    private void createAdminUser() {
        if (!userRepository.existsByEmail("admin@collabry.com")) {
            userRepository.save(new User("admin@collabry.com",
                    passwordEncoder.encode("password123"), Role.ADMIN));
        }
    }

    private void createBrandUser() {
        if (!userRepository.existsByEmail("brand@collabry.com")) {
            userRepository.save(new User("brand@collabry.com",
                    passwordEncoder.encode("password123"), Role.BRAND));
        }
    }

    private User getOrCreateInfluencerUser() {
        if (!userRepository.existsByEmail("influencer@collabry.com")) {
            User influencer = new User("influencer@collabry.com",
                    passwordEncoder.encode("password123"), Role.INFLUENCER);
            return userRepository.save(influencer);
        }
        return userRepository.findByEmail("influencer@collabry.com").orElse(null);
    }

    private void upsertInfluencerProfile(User influencerUser) {
        InfluencerProfile profile = influencerProfileRepository
                .findByUserId(influencerUser.getId())
                .orElseGet(InfluencerProfile::new);
        profile.setUserId(influencerUser.getId());
        if (profile.getName() == null) profile.setName("Test Influencer");
        if (profile.getAge() == null) profile.setAge(SEED_INFLUENCER_AGE);
        if (profile.getLocation() == null) profile.setLocation("Halifax");
        if (profile.getNiche() == null) profile.setNiche("Fashion");
        if (profile.getBio() == null) profile.setBio("Fashion and lifestyle content creator");
        if (profile.getInstagramHandle() == null) profile.setInstagramHandle("@testinfluencer");
        if (profile.getRate() == null) profile.setRate(SEED_INFLUENCER_RATE);
        if (profile.getFollowerCount() == null) profile.setFollowerCount(SEED_INFLUENCER_FOLLOWERS);
        if (profile.getEngagementRate() == null) profile.setEngagementRate(SEED_INFLUENCER_ENGAGEMENT);
        profile.setComplete(true);
        influencerProfileRepository.save(profile);
    }
}
