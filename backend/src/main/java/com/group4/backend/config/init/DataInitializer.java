package com.group4.backend.config.init;

import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class DataInitializer {

    private static final int SEED_INFLUENCER_AGE = 25;
    private static final long SEED_INFLUENCER_FOLLOWERS = 125_000L;
    private static final java.math.BigDecimal SEED_INFLUENCER_RATE = java.math.BigDecimal.valueOf(1200);
    private static final java.math.BigDecimal SEED_INFLUENCER_ENGAGEMENT = java.math.BigDecimal.valueOf(4.8);

    private static final String SEED_INFLUENCER_NAME      = "Alex Rivera";
    private static final String SEED_INFLUENCER_LOCATION  = "Toronto, ON";
    private static final String SEED_INFLUENCER_NICHE     = "Fashion";
    private static final String SEED_INFLUENCER_BIO       =
            "Lifestyle and fashion content creator helping brands tell authentic stories. "
            + "Partnered with 30+ brands across North America.";
    private static final String SEED_INFLUENCER_INSTAGRAM = "@alexrivera.creates";
    private static final String SEED_INFLUENCER_TIKTOK    = "@alexrivera";
    private static final String SEED_BRAND_DESCRIPTION    =
            "A leading consumer lifestyle brand partnering with top influencers across fashion, "
            + "tech, and fitness to reach engaged audiences worldwide.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final BrandProfileRepository brandProfileRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           InfluencerProfileRepository influencerProfileRepository,
                           BrandProfileRepository brandProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.influencerProfileRepository = influencerProfileRepository;
        this.brandProfileRepository = brandProfileRepository;
    }

    @Bean
    @Order(1)
    public CommandLineRunner initializeData() {
        return args -> {
            createAdminUser();
            User brandUser = getOrCreateBrandUser();
            upsertBrandProfile(brandUser);
            User influencerUser = getOrCreateInfluencerUser();
            upsertInfluencerProfile(influencerUser);
        };
    }

    private void createAdminUser() {
        if (!userRepository.existsByEmail("admin@collabry.com")) {
            User admin = new User("admin@collabry.com",
                    passwordEncoder.encode("password123"), Role.ADMIN);
            admin.setVerified(true);
            userRepository.save(admin);
        }
    }

    private User getOrCreateBrandUser() {
        if (!userRepository.existsByEmail("brand@collabry.com")) {
            User brand = new User("brand@collabry.com",
                    passwordEncoder.encode("password123"), Role.BRAND);
            brand.setVerified(false);
            return userRepository.save(brand);
        }
        return userRepository.findByEmail("brand@collabry.com").orElseThrow();
    }

    private void upsertBrandProfile(User brandUser) {
        if (brandProfileRepository.findByUserId(brandUser.getId()).isPresent()) return;

        BrandProfile profile = new BrandProfile();
        profile.setUserId(brandUser.getId());
        profile.setEmail("brand@collabry.com");
        profile.setName("Collabry Demo Brand");
        profile.setDescription(SEED_BRAND_DESCRIPTION);
        profile.setIndustry("Consumer Lifestyle");
        profile.setWebsite("https://collabry.com");
        profile.setVerified(false);
        brandProfileRepository.save(profile);
    }

    private User getOrCreateInfluencerUser() {
        if (!userRepository.existsByEmail("influencer@collabry.com")) {
            User influencer = new User("influencer@collabry.com",
                    passwordEncoder.encode("password123"), Role.INFLUENCER);
            influencer.setVerified(false);
            return userRepository.save(influencer);
        }
        return userRepository.findByEmail("influencer@collabry.com").orElseThrow();
    }

    private void upsertInfluencerProfile(User influencerUser) {
        InfluencerProfile profile = influencerProfileRepository
                .findByUserId(influencerUser.getId())
                .orElseGet(InfluencerProfile::new);
        profile.setUserId(influencerUser.getId());
        applyInfluencerDefaults(profile);
        profile.setComplete(true);
        profile.setOpenToCollaborations(true);
        influencerProfileRepository.save(profile);
    }

    private void applyInfluencerDefaults(InfluencerProfile profile) {
        applyInfluencerIdentityDefaults(profile);
        applyInfluencerSocialAndMetricsDefaults(profile);
    }

    private void applyInfluencerIdentityDefaults(InfluencerProfile profile) {
        if (profile.getName() == null) profile.setName(SEED_INFLUENCER_NAME);
        if (profile.getAge() == null) profile.setAge(SEED_INFLUENCER_AGE);
        if (profile.getLocation() == null) profile.setLocation(SEED_INFLUENCER_LOCATION);
        if (profile.getNiche() == null) profile.setNiche(SEED_INFLUENCER_NICHE);
        if (profile.getBio() == null) profile.setBio(SEED_INFLUENCER_BIO);
    }

    private void applyInfluencerSocialAndMetricsDefaults(InfluencerProfile profile) {
        if (profile.getInstagramHandle() == null) profile.setInstagramHandle(SEED_INFLUENCER_INSTAGRAM);
        if (profile.getTiktokHandle() == null) profile.setTiktokHandle(SEED_INFLUENCER_TIKTOK);
        if (profile.getRate() == null) profile.setRate(SEED_INFLUENCER_RATE);
        if (profile.getFollowerCount() == null) profile.setFollowerCount(SEED_INFLUENCER_FOLLOWERS);
        if (profile.getEngagementRate() == null) profile.setEngagementRate(SEED_INFLUENCER_ENGAGEMENT);
    }
}
