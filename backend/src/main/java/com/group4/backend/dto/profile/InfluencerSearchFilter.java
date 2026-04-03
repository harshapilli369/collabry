package com.group4.backend.dto.profile;

import java.math.BigDecimal;

/**
 * Encapsulates all filter criteria for influencer search queries,
 * replacing the six-parameter list used across the search chain.
 */
public record InfluencerSearchFilter(
        String niche,
        String location,
        Long minFollowers,
        Long maxFollowers,
        BigDecimal minEngagementRate,
        Boolean availableOnly
) {}
