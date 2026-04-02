package com.group4.backend.service.profile;

import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.model.InfluencerProfile;

/**
 * Relevance scoring for brand influencer search. Higher score = more relevant to the query and filters.
 * Used after DB filtering so indexes can support predicates; ordering is applied in memory.
 */
public final class InfluencerSearchRanker {

    private static final double NICHE_EXACT = 1000;
    private static final double NICHE_PREFIX = 500;
    private static final double NICHE_SUBSTRING = 250;
    private static final double LOCATION_MATCH = 100;
    private static final double MAX_ENGAGEMENT_BONUS = 50;
    private static final double MAX_FOLLOWER_BONUS = 30;
    private static final double MAX_CENTER_BAND_BONUS = 40;
    private static final double ENGAGEMENT_RATE_MULTIPLIER = 2.0;
    private static final double FOLLOWER_SCALE_DIVISOR = 5000.0;
    private static final double MIDPOINT_DIVISOR = 2.0;

    private InfluencerSearchRanker() {
    }

    /**
     * @param filter search criteria containing niche, location, and follower bounds used for scoring
     */
    public static double relevanceScore(InfluencerProfile p, InfluencerSearchFilter filter) {
        return nicheScore(p, filter.niche())
                + locationScore(p, filter.location())
                + engagementScore(p)
                + followerScore(p)
                + followerCenterScore(p, filter.minFollowers(), filter.maxFollowers());
    }

    private static double nicheScore(InfluencerProfile p, String nicheQuery) {
        String nq = nicheQuery != null ? nicheQuery.trim().toLowerCase() : "";
        if (nq.isEmpty()) return 0;
        String niche = p.getNiche() != null ? p.getNiche().toLowerCase() : "";
        if (niche.equals(nq)) return NICHE_EXACT;
        if (niche.startsWith(nq)) return NICHE_PREFIX;
        if (niche.contains(nq)) return NICHE_SUBSTRING;
        return 0;
    }

    private static double locationScore(InfluencerProfile p, String locationQuery) {
        String lq = locationQuery != null ? locationQuery.trim().toLowerCase() : "";
        if (lq.isEmpty()) return 0;
        String loc = p.getLocation() != null ? p.getLocation().toLowerCase() : "";
        return loc.contains(lq) ? LOCATION_MATCH : 0;
    }

    private static double engagementScore(InfluencerProfile p) {
        if (p.getEngagementRate() == null) return 0;
        return Math.min(p.getEngagementRate().doubleValue() * ENGAGEMENT_RATE_MULTIPLIER, MAX_ENGAGEMENT_BONUS);
    }

    private static double followerScore(InfluencerProfile p) {
        if (p.getFollowerCount() == null) return 0;
        return Math.min(p.getFollowerCount() / FOLLOWER_SCALE_DIVISOR, MAX_FOLLOWER_BONUS);
    }

    private static boolean hasFollowerBounds(InfluencerProfile p, Long minFollowers, Long maxFollowers) {
        return minFollowers != null && maxFollowers != null && p.getFollowerCount() != null;
    }

    private static double followerCenterScore(InfluencerProfile p, Long minFollowers, Long maxFollowers) {
        if (!hasFollowerBounds(p, minFollowers, maxFollowers)) return 0;
        double mid = (minFollowers + maxFollowers) / MIDPOINT_DIVISOR;
        double dist = Math.abs(p.getFollowerCount() - mid);
        double span = Math.max(maxFollowers - minFollowers, 1L);
        double closeness = 1.0 - Math.min(dist / span, 1.0);
        return closeness * MAX_CENTER_BAND_BONUS;
    }
}
