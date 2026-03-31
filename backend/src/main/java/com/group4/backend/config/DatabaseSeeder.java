package com.group4.backend.config;

import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.BrandProfileRepository;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final int SEED_INFLUENCER_AGE = 26;

    private final UserRepository userRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          InfluencerProfileRepository influencerProfileRepository,
                          BrandProfileRepository brandProfileRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.influencerProfileRepository = influencerProfileRepository;
        this.brandProfileRepository = brandProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Starting Database Seeder (Upsert Mode)...");
        seedBrandUser();
        seedInfluencers();
        verifyUnverifiedBrands();
        System.out.println("Database Seeder check completed.");
    }

    private void seedBrandUser() {
        if (userRepository.existsByEmail("admin@brand.com")) return;
        User brandUser = new User("admin@brand.com", passwordEncoder.encode("password123"), Role.BRAND);
        brandUser.setVerified(true);
        brandUser = userRepository.save(brandUser);

        BrandProfile brandProfile = new BrandProfile();
        brandProfile.setUserId(brandUser.getId());
        brandProfile.setEmail("admin@brand.com");
        brandProfile.setName("Tech Haven & Co.");
        brandProfile.setDescription("Premium electronics & lifestyle accessories for the modern world.");
        brandProfile.setIndustry("Technology");
        brandProfile.setWebsite("https://techhaven.com");
        brandProfile.setVerified(true);
        brandProfileRepository.save(brandProfile);
        System.out.println(" => Seeded Brand: admin@brand.com");
    }

    private void seedInfluencers() {
        String password = passwordEncoder.encode("password123");
        for (MockInfluencer m : buildMockInfluencers()) {
            if (userRepository.existsByEmail(m.email())) continue;
            User u = new User(m.email(), password, Role.INFLUENCER);
            u.setVerified(true);
            u = userRepository.save(u);
            saveInfluencerProfile(u.getId(), m);
            System.out.println(" => Seeded Influencer: " + m.email() + " (" + m.niche() + ")");
        }
    }

    private void saveInfluencerProfile(Long userId, MockInfluencer m) {
        InfluencerProfile p = new InfluencerProfile();
        p.setUserId(userId);
        p.setName(m.name());
        p.setAge(SEED_INFLUENCER_AGE);
        p.setNiche(m.niche());
        p.setBio(m.bio());
        p.setLocation(m.location());
        p.setAudienceInfo(m.audienceInfo());
        p.setRate(BigDecimal.valueOf(m.rate()));
        p.setProfilePictureUrl(m.picUrl());
        p.setOpenToCollaborations(true);
        influencerProfileRepository.save(p);
    }

    private void verifyUnverifiedBrands() {
        for (User u : userRepository.findAll()) {
            if (u.getRole() == Role.BRAND && !u.isVerified()) {
                u.setVerified(true);
                userRepository.save(u);
                System.out.println(" => Verified brand: " + u.getEmail());
            }
        }
    }

    private static List<MockInfluencer> buildMockInfluencers() {
        return Arrays.asList(
            new MockInfluencer("gamer1@test.com", "Taylor", "Gaming",
                "Esports champion and enthusiastic PC hardware reviewer.", "Seattle, WA",
                "1.2M YouTube, 500k Twitch", 5000.0, "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("gamer2@test.com", "Jordan (Retro)", "Gaming",
                "Retro gaming console collector and passionate streamer.", "Austin, TX",
                "300k Twitch", 1500.0, "https://images.unsplash.com/photo-1548681528-6a5c45b66b42?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("gamer3@test.com", "Alex FPS", "Gaming",
                "Competitive FPS player ranking top 10 globally.", "Los Angeles, CA",
                "800k TikTok, 2M YouTube", 4000.0, "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("tech1@test.com", "Sam Gadgets", "Technology",
                "Unboxing the newest smartphones and wearables.", "San Francisco, CA",
                "3M YouTube", 10000.0, "https://images.unsplash.com/photo-1527980965255-d3b416303d12?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("tech2@test.com", "Morgan Codes", "Technology",
                "Software engineer sharing coding tips and setup aesthetics.", "New York, NY",
                "400k Instagram, 100k Twitter", 2000.0, "https://images.unsplash.com/photo-1554151228-14d9def656e4?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("tech3@test.com", "Riley AI", "Technology",
                "Deep dives into artificial intelligence and smart home tech.", "London, UK",
                "1.5M YouTube, 900k TikTok", 7500.0, "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("fit1@test.com", "Casey Lifts", "Fitness",
                "Powerlifting coach and nutrition expert.", "Denver, CO",
                "1M Instagram", 3500.0, "https://images.unsplash.com/photo-1517849845537-4d257902454a?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("fit2@test.com", "Avery Yoga", "Fitness",
                "Mindful living and advanced Yoga practitioner.", "San Diego, CA",
                "500k TikTok", 1800.0, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("fit3@test.com", "Drew Runner", "Fitness",
                "Marathon runner sharing daily motivation and endurance tips.", "Boston, MA",
                "850k YouTube", 3000.0, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("style1@test.com", "Blake Couture", "Fashion",
                "High-end fashion editorial photographer and model.", "Paris, FR",
                "2.2M Instagram", 8000.0, "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("style2@test.com", "Jamie Streetwear", "Fashion",
                "Urban fashion trends, sneaker collector.", "Tokyo, JP",
                "1.1M TikTok", 4000.0, "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200&h=200&fit=crop"),
            new MockInfluencer("style3@test.com", "Robin Vintage", "Fashion",
                "Thrifting and sustainable vintage style enthusiast.", "Portland, OR",
                "450k Instagram", 1500.0, "https://images.unsplash.com/photo-1513379733131-47fc74b45fc7?q=80&w=200&h=200&fit=crop")
        );
    }

    private record MockInfluencer(String email, String name, String niche, String bio,
                                   String location, String audienceInfo, Double rate, String picUrl) {}
}
