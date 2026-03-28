package com.group4.backend.service;

import com.group4.backend.model.InfluencerProfile;

import java.math.BigDecimal;

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

    private InfluencerSearchRanker() {
    }

    /**
     * @param nicheQuery    optional niche filter text (same semantics as search LIKE)
     * @param locationQuery optional location filter text
     * @param minFollowers  optional lower follower bound (must pair with max for centering bonus)
     * @param maxFollowers  optional upper follower bound
     */
    public static double relevanceScore(InfluencerProfile p, String nicheQuery, String locationQuery,
                                        Long minFollowers, Long maxFollowers) {
        double score = 0;
        String nq = nicheQuery != null ? nicheQuery.trim().toLowerCase() : "";
        if (!nq.isEmpty()) {
            String niche = p.getNiche() != null ? p.getNiche().toLowerCase() : "";
            if (niche.equals(nq)) {
                score += NICHE_EXACT;
            } else if (niche.startsWith(nq)) {
                score += NICHE_PREFIX;
            } else if (niche.contains(nq)) {
                score += NICHE_SUBSTRING;
            }
        }

        String lq = locationQuery != null ? locationQuery.trim().toLowerCase() : "";
        if (!lq.isEmpty()) {
            String loc = p.getLocation() != null ? p.getLocation().toLowerCase() : "";
            if (loc.contains(lq)) {
                score += LOCATION_MATCH;
            }
        }

        if (p.getEngagementRate() != null) {
            double er = p.getEngagementRate().doubleValue();
            score += Math.min(er * 2.0, MAX_ENGAGEMENT_BONUS);
        }
        if (p.getFollowerCount() != null) {
            score += Math.min(p.getFollowerCount() / 5000.0, MAX_FOLLOWER_BONUS);
        }

        if (minFollowers != null && maxFollowers != null && p.getFollowerCount() != null) {
            double mid = (minFollowers + maxFollowers) / 2.0;
            double dist = Math.abs(p.getFollowerCount() - mid);
            double span = Math.max(maxFollowers - minFollowers, 1L);
            double closeness = 1.0 - Math.min(dist / span, 1.0);
            score += closeness * MAX_CENTER_BAND_BONUS;
        }

        return score;
    }
}
