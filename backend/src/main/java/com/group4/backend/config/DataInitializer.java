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

import java.math.BigDecimal;
import java.util.Optional;

@Configuration
public class DataInitializer {

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
            // Create Admin User if not exists
            if (!userRepository.existsByEmail("admin@collabry.com")) {
                User admin = new User(
                        "admin@collabry.com",
                        passwordEncoder.encode("password123"),
                        Role.ADMIN);
                userRepository.save(admin);
            }

            // Create test Brand user for payment testing
            if (!userRepository.existsByEmail("brand@collabry.com")) {
                User brand = new User(
                        "brand@collabry.com",
                        passwordEncoder.encode("password123"),
                        Role.BRAND);
                userRepository.save(brand);
            }

            // Create test Influencer user for payment testing
            User influencerUser = null;
            if (!userRepository.existsByEmail("influencer@collabry.com")) {
                User influencer = new User(
                        "influencer@collabry.com",
                        passwordEncoder.encode("password123"),
                        Role.INFLUENCER);
                influencerUser = userRepository.save(influencer);
            } else {
                influencerUser = userRepository.findByEmail("influencer@collabry.com").orElse(null);
            }

            // Ensure complete InfluencerProfile for influencer so brand search returns results
            if (influencerUser != null) {
                Optional<InfluencerProfile> existing = influencerProfileRepository.findByUserId(influencerUser.getId());
                InfluencerProfile profile = existing.orElseGet(InfluencerProfile::new);
                profile.setUserId(influencerUser.getId());
                profile.setName(profile.getName() != null ? profile.getName() : "Test Influencer");
                profile.setAge(profile.getAge() != null ? profile.getAge() : 25);
                profile.setLocation(profile.getLocation() != null ? profile.getLocation() : "Halifax");
                profile.setNiche(profile.getNiche() != null ? profile.getNiche() : "Fashion");
                if (profile.getBio() == null) profile.setBio("Fashion and lifestyle content creator");
                if (profile.getInstagramHandle() == null) profile.setInstagramHandle("@testinfluencer");
                if (profile.getRate() == null) profile.setRate(BigDecimal.valueOf(500));
                if (profile.getFollowerCount() == null) profile.setFollowerCount(5000L);
                if (profile.getEngagementRate() == null) profile.setEngagementRate(BigDecimal.valueOf(4.5));
                profile.setComplete(true);
                influencerProfileRepository.save(profile);
            }
        };
    }
}
