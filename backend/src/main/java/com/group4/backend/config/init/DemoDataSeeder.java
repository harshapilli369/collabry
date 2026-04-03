package com.group4.backend.config.init;

import com.group4.backend.model.*;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.payment.PaymentRepository;
import com.group4.backend.repository.profile.InfluencerRatingRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Seeds realistic demo campaigns, invitations, payments, and ratings so the
 * application looks active on first launch against a fresh database.
 *
 * Runs after DataInitializer (Order 1) and DatabaseSeeder (Order 2) so all
 * users are guaranteed to exist.
 */
@Order(3)
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String BRAND_EMAIL       = "brand@collabry.com";
    private static final String INFLUENCER_EMAIL  = "influencer@collabry.com";

    // Demo data dates (year and key month/day values)
    private static final int DEMO_YEAR                   = 2026;
    private static final int DEMO_Q1_LAUNCH_START_MONTH  = 1;
    private static final int DEMO_Q1_LAUNCH_START_DAY    = 10;
    private static final int DEMO_Q1_LAUNCH_END_MONTH    = 3;
    private static final int DEMO_Q1_LAUNCH_END_DAY      = 15;
    private static final int DEMO_INV1A_EXPIRY_OFFSET    = -60;
    private static final int DEMO_INV1A_RESPONDED_OFFSET = -30;
    private static final int DEMO_INV1B_EXPIRY_OFFSET    = -65;
    private static final int DEMO_INV1B_RESPONDED_OFFSET = -35;
    private static final int DEMO_PAY1A_DUE_MONTH        = 3;
    private static final int DEMO_PAY1A_DUE_DAY          = 1;
    private static final int DEMO_PAY1A_PAID_DAY         = 5;
    private static final int DEMO_PAY1B_DUE_DAY          = 10;
    private static final int DEMO_PAY1B_PAID_DAY         = 12;
    private static final int DEMO_SPRING_START_MONTH     = 3;
    private static final int DEMO_SPRING_START_DAY       = 1;
    private static final int DEMO_SPRING_END_MONTH       = 5;
    private static final int DEMO_SPRING_END_DAY         = 31;
    private static final int DEMO_INV_SPRING_A_OFFSET    = -10;
    private static final int DEMO_INV_SPRING_B_OFFSET    = -12;
    private static final int DEMO_INV_SPRING_C_OFFSET    = 7;
    private static final int DEMO_PAY_SPRING_DUE_DAY     = 5;
    private static final int DEMO_PAY_SPRING_PAID_DAY    = 6;
    private static final int DEMO_PAY_FINAL_DUE_MONTH    = 4;
    private static final int DEMO_PAY_FINAL_DUE_DAY      = 30;
    private static final int DEMO_GAMING_START_MONTH     = 4;
    private static final int DEMO_GAMING_START_DAY       = 1;
    private static final int DEMO_GAMING_END_MONTH       = 6;
    private static final int DEMO_GAMING_END_DAY         = 30;
    private static final int DEMO_INV_GAMING_OFFSET      = 5;

    // Influencers created by DatabaseSeeder that we link to demo campaigns
    private static final String TECH1_EMAIL   = "tech1@test.com";
    private static final String STYLE1_EMAIL  = "style1@test.com";
    private static final String GAMER1_EMAIL  = "gamer1@test.com";
    private static final String FIT1_EMAIL    = "fit1@test.com";

    /** Target influencer headcount on seeded campaigns (demo narrative). */
    private static final int DEMO_Q1_CAMPAIGN_INFLUENCER_SLOTS = 3;
    private static final int DEMO_SPRING_CAMPAIGN_INFLUENCER_SLOTS = 4;
    private static final int DEMO_GAMING_CAMPAIGN_INFLUENCER_SLOTS = 5;
    private static final int DEMO_RATING_STARS_MAX = 5;

    private final UserRepository           userRepository;
    private final CampaignRepository       campaignRepository;
    private final InvitationRepository     invitationRepository;
    private final PaymentRepository        paymentRepository;
    private final InfluencerRatingRepository ratingRepository;

    public DemoDataSeeder(UserRepository userRepository,
                          CampaignRepository campaignRepository,
                          InvitationRepository invitationRepository,
                          PaymentRepository paymentRepository,
                          InfluencerRatingRepository ratingRepository) {
        this.userRepository    = userRepository;
        this.campaignRepository = campaignRepository;
        this.invitationRepository = invitationRepository;
        this.paymentRepository  = paymentRepository;
        this.ratingRepository   = ratingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("Starting Demo Data Seeder...");

        Optional<User> brandOpt = userRepository.findByEmail(BRAND_EMAIL);
        if (brandOpt.isEmpty()) {
            System.out.println(" => Demo Data Seeder skipped: brand user not found.");
            return;
        }
        User brand = brandOpt.get();

        // Skip entirely if campaigns already exist for this brand
        if (!campaignRepository.findByUserIdOrderByCreatedAtDesc(brand.getId()).isEmpty()) {
            System.out.println(" => Demo Data Seeder skipped: campaigns already exist.");
            return;
        }

        Long influencerId = userId(INFLUENCER_EMAIL);
        Long tech1Id      = userId(TECH1_EMAIL);
        Long style1Id     = userId(STYLE1_EMAIL);
        Long gamer1Id     = userId(GAMER1_EMAIL);
        Long fit1Id       = userId(FIT1_EMAIL);

        // ── Campaign 1: COMPLETED ─────────────────────────────────────────────
        Campaign q1Launch = saveCampaign(brand.getId(), new CampaignData(
                "Q1 Tech Product Launch",
                "Drive awareness for our new smart home accessory line targeting early adopters and tech enthusiasts.",
                BudgetRange.FIVE_K_10K, CampaignStatus.COMPLETED, CampaignGoal.AWARENESS,
                "Instagram Reels, YouTube Review",
                LocalDate.of(DEMO_YEAR, DEMO_Q1_LAUNCH_START_MONTH, DEMO_Q1_LAUNCH_START_DAY),
                LocalDate.of(DEMO_YEAR, DEMO_Q1_LAUNCH_END_MONTH, DEMO_Q1_LAUNCH_END_DAY),
                DEMO_Q1_CAMPAIGN_INFLUENCER_SLOTS));

        // Invitations for Campaign 1
        CollaborationInvitation inv1a = saveInvitation(q1Launch.getId(), influencerId, brand.getId(),
                new InvitationData(InvitationStatus.CONFIRMED,
                        "Hi Alex! We'd love for you to feature our smart home hub in a lifestyle reel.",
                        new BigDecimal("1200.00"), "4 weeks", "1x Instagram Reel, 3x Stories", "Instagram",
                        DEMO_INV1A_EXPIRY_OFFSET, DEMO_INV1A_RESPONDED_OFFSET));

        CollaborationInvitation inv1b = saveInvitation(q1Launch.getId(), tech1Id, brand.getId(),
                new InvitationData(InvitationStatus.CONFIRMED,
                        "Sam, your audience is a perfect fit for our tech launch. Would love a detailed YouTube review.",
                        new BigDecimal("9500.00"), "6 weeks", "1x YouTube Review (min 10 min), 1x Community Post",
                        "YouTube", DEMO_INV1B_EXPIRY_OFFSET, DEMO_INV1B_RESPONDED_OFFSET));

        // Payments for Campaign 1 (PAID)
        savePayment(q1Launch.getId(), influencerId, brand.getId(),
                new PaymentData("Content Delivery", new BigDecimal("1200.00"), PaymentStatus.PAID,
                        LocalDate.of(DEMO_YEAR, DEMO_PAY1A_DUE_MONTH, DEMO_PAY1A_DUE_DAY),
                        LocalDate.of(DEMO_YEAR, DEMO_PAY1A_DUE_MONTH, DEMO_PAY1A_PAID_DAY),
                        "INV-2026-0001", "Payment for Instagram Reel and Stories — Q1 Tech Launch"));

        savePayment(q1Launch.getId(), tech1Id, brand.getId(),
                new PaymentData("YouTube Review Delivery", new BigDecimal("9500.00"), PaymentStatus.PAID,
                        LocalDate.of(DEMO_YEAR, DEMO_PAY1A_DUE_MONTH, DEMO_PAY1B_DUE_DAY),
                        LocalDate.of(DEMO_YEAR, DEMO_PAY1A_DUE_MONTH, DEMO_PAY1B_PAID_DAY),
                        "INV-2026-0002", "Payment for YouTube review — Q1 Tech Launch"));

        // Ratings for Campaign 1
        saveRating(inv1a.getId(), brand.getId(), influencerId, DEMO_RATING_STARS_MAX,
                "Alex delivered exceptional content — on time, on brief, and the engagement was incredible. Will definitely work together again.");

        saveRating(inv1b.getId(), brand.getId(), tech1Id, DEMO_RATING_STARS_MAX,
                "Sam's review was thorough and authentic. Our product page traffic spiked 40% the week the video went live.");

        // ── Campaign 2: ACTIVE ────────────────────────────────────────────────
        Campaign springFashion = saveCampaign(brand.getId(), new CampaignData(
                "Spring Fashion Collection",
                "Showcase our new spring collection to fashion-forward audiences ahead of the season launch.",
                BudgetRange.ONE_K_5K, CampaignStatus.ACTIVE, CampaignGoal.ENGAGEMENT,
                "Instagram Posts, TikTok",
                LocalDate.of(DEMO_YEAR, DEMO_SPRING_START_MONTH, DEMO_SPRING_START_DAY),
                LocalDate.of(DEMO_YEAR, DEMO_SPRING_END_MONTH, DEMO_SPRING_END_DAY),
                DEMO_SPRING_CAMPAIGN_INFLUENCER_SLOTS));

        saveInvitation(springFashion.getId(), influencerId, brand.getId(),
                new InvitationData(InvitationStatus.ACCEPTED,
                        "Alex, your aesthetic is exactly what we're looking for this season. Let's create something beautiful together!",
                        new BigDecimal("1200.00"), "3 weeks", "2x Instagram Posts, 1x TikTok", "Instagram",
                        DEMO_INV_SPRING_A_OFFSET, null));

        saveInvitation(springFashion.getId(), style1Id, brand.getId(),
                new InvitationData(InvitationStatus.ACCEPTED,
                        "Blake, we'd love for you to style our new pieces for your editorial audience.",
                        new BigDecimal("7500.00"), "4 weeks", "3x Instagram Posts, 1x Reel", "Instagram",
                        DEMO_INV_SPRING_B_OFFSET, null));

        saveInvitation(springFashion.getId(), fit1Id, brand.getId(),
                new InvitationData(InvitationStatus.PENDING,
                        "Casey, we're exploring an activewear crossover with our spring line — interested?",
                        new BigDecimal("3000.00"), "3 weeks", "2x Instagram Posts", "Instagram",
                        DEMO_INV_SPRING_C_OFFSET, null));

        // Milestone payment for Campaign 2 (in progress)
        savePayment(springFashion.getId(), influencerId, brand.getId(),
                new PaymentData("50% Upfront Deposit", new BigDecimal("600.00"), PaymentStatus.PAID,
                        LocalDate.of(DEMO_YEAR, DEMO_SPRING_START_MONTH, DEMO_PAY_SPRING_DUE_DAY),
                        LocalDate.of(DEMO_YEAR, DEMO_SPRING_START_MONTH, DEMO_PAY_SPRING_PAID_DAY),
                        "INV-2026-0003", "50% upfront deposit — Spring Fashion Collection"));

        savePayment(springFashion.getId(), influencerId, brand.getId(),
                new PaymentData("Final Delivery Payment", new BigDecimal("600.00"), PaymentStatus.PENDING,
                        LocalDate.of(DEMO_YEAR, DEMO_PAY_FINAL_DUE_MONTH, DEMO_PAY_FINAL_DUE_DAY), null,
                        "INV-2026-0004", "Final payment on content delivery — Spring Fashion Collection"));

        // ── Campaign 3: ACTIVE (Gaming) ───────────────────────────────────────
        Campaign gamingCampaign = saveCampaign(brand.getId(), new CampaignData(
                "Gaming Accessories Launch",
                "Reach competitive gamers and streamers with our new high-performance peripherals lineup.",
                BudgetRange.TEN_K_50K, CampaignStatus.ACTIVE, CampaignGoal.CONVERSIONS,
                "YouTube, Twitch, TikTok",
                LocalDate.of(DEMO_YEAR, DEMO_GAMING_START_MONTH, DEMO_GAMING_START_DAY),
                LocalDate.of(DEMO_YEAR, DEMO_GAMING_END_MONTH, DEMO_GAMING_END_DAY),
                DEMO_GAMING_CAMPAIGN_INFLUENCER_SLOTS));

        saveInvitation(gamingCampaign.getId(), gamer1Id, brand.getId(),
                new InvitationData(InvitationStatus.PENDING,
                        "Taylor, your Twitch reach and authentic reviews are exactly what our gaming peripherals campaign needs.",
                        new BigDecimal("5000.00"), "4 weeks", "1x Sponsored Stream (2h+), 1x YouTube Integration",
                        "Twitch", DEMO_INV_GAMING_OFFSET, null));

        System.out.println(" => Demo campaigns, invitations, payments, and ratings seeded successfully.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long userId(String email) {
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }

    private Campaign saveCampaign(Long brandUserId, CampaignData data) {
        Campaign c = new Campaign();
        c.setUserId(brandUserId);
        c.setName(data.name());
        c.setDescription(data.description());
        c.setBudgetRange(data.budget());
        c.setStatus(data.status());
        c.setCampaignGoal(data.goal());
        c.setPreferredContentTypes(data.contentTypes());
        c.setStartDate(data.start());
        c.setEndDate(data.end());
        c.setNumberOfInfluencers(data.numInfluencers());
        return campaignRepository.save(c);
    }

    /**
     * @param data.expiryOffsetDays   days from now for expiry (positive = future, negative = past)
     * @param data.respondedOffsetDays days from now respondedAt was set, or null if not yet responded
     */
    private CollaborationInvitation saveInvitation(Long campaignId, Long influencerId, Long brandId,
                                                    InvitationData data) {
        if (influencerId == null) return new CollaborationInvitation();

        CollaborationInvitation inv = new CollaborationInvitation();
        inv.setCampaignId(campaignId);
        inv.setInfluencerId(influencerId);
        inv.setBrandId(brandId);
        inv.setStatus(data.status());
        inv.setBrandMessage(data.message());
        inv.setProposedAmount(data.amount());
        inv.setProposedTimeline(data.timeline());
        inv.setProposedDeliverables(data.deliverables());
        inv.setPlatform(data.platform());
        inv.setExpiresAt(Instant.now().plus(data.expiryOffsetDays(), ChronoUnit.DAYS));
        if (data.respondedOffsetDays() != null) {
            inv.setRespondedAt(Instant.now().plus(data.respondedOffsetDays(), ChronoUnit.DAYS));
        }
        return invitationRepository.save(inv);
    }

    private void savePayment(Long campaignId, Long influencerId, Long brandId, PaymentData data) {
        if (influencerId == null) return;
        if (paymentInvoiceExistsForCampaign(campaignId, data.invoiceNumber())) return;

        Payment p = new Payment();
        p.setCampaignId(campaignId);
        p.setInfluencerId(influencerId);
        p.setBrandId(brandId);
        p.setMilestoneName(data.milestoneName());
        p.setAmount(data.amount());
        p.setStatus(data.status());
        p.setDueDate(data.dueDate());
        p.setPaidDate(data.paidDate());
        p.setInvoiceNumber(data.invoiceNumber());
        p.setNotes(data.notes());
        paymentRepository.save(p);
    }

    private boolean paymentInvoiceExistsForCampaign(Long campaignId, String invoiceNumber) {
        return paymentRepository.findByCampaignIdOrderByDueDateAsc(campaignId).stream()
                .anyMatch(p -> p.getInvoiceNumber().equals(invoiceNumber));
    }

    private void saveRating(Long invitationId, Long brandId, Long influencerId,
                             int rating, String review) {
        if (influencerId == null) return;
        if (ratingRepository.findByInvitationId(invitationId).isPresent()) return;

        InfluencerRating r = new InfluencerRating();
        r.setInvitationId(invitationId);
        r.setBrandId(brandId);
        r.setInfluencerId(influencerId);
        r.setRating(rating);
        r.setReview(review);
        ratingRepository.save(r);
    }

    // ── Parameter objects ─────────────────────────────────────────────────────

    private record CampaignData(
            String name, String description, BudgetRange budget, CampaignStatus status,
            CampaignGoal goal, String contentTypes, LocalDate start, LocalDate end, int numInfluencers) {}

    private record InvitationData(
            InvitationStatus status, String message, BigDecimal amount,
            String timeline, String deliverables, String platform,
            int expiryOffsetDays, Integer respondedOffsetDays) {}

    private record PaymentData(
            String milestoneName, BigDecimal amount, PaymentStatus status,
            LocalDate dueDate, LocalDate paidDate, String invoiceNumber, String notes) {}
}
