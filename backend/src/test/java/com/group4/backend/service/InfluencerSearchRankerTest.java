package com.group4.backend.service;
import com.group4.backend.service.profile.InfluencerSearchRanker;

import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.model.InfluencerProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD for influencer search relevance: niche match quality and stats-based ordering.
 */
class InfluencerSearchRankerTest {

    @Test
    void exactNicheMatch_scoresHigherThanSubstringMatch() {
        InfluencerProfile exact = profile("Gaming", "Austin", 10_000L, BigDecimal.valueOf(3.0));
        InfluencerProfile partial = profile("Retro Gaming", "Austin", 10_000L, BigDecimal.valueOf(3.0));

        double sExact = InfluencerSearchRanker.relevanceScore(exact, new InfluencerSearchFilter("gaming", null, null, null, null, null));
        double sPartial = InfluencerSearchRanker.relevanceScore(partial, new InfluencerSearchFilter("gaming", null, null, null, null, null));

        assertThat(sExact).isGreaterThan(sPartial);
    }

    @Test
    void prefixNicheMatch_scoresHigherThanSubstringOnly() {
        InfluencerProfile prefix = profile("Fashion", "Paris", 5_000L, BigDecimal.valueOf(4.0));
        InfluencerProfile containsOnly = profile("Sustainable Fashion", "Paris", 5_000L, BigDecimal.valueOf(4.0));

        double sPrefix = InfluencerSearchRanker.relevanceScore(prefix, new InfluencerSearchFilter("fash", null, null, null, null, null));
        double sContains = InfluencerSearchRanker.relevanceScore(containsOnly, new InfluencerSearchFilter("fash", null, null, null, null, null));

        assertThat(sPrefix).isGreaterThan(sContains);
    }

    @Test
    void locationQuery_boostsWhenLocationContainsTerm() {
        InfluencerProfile inNy = profile("Tech", "New York, NY", 20_000L, BigDecimal.valueOf(5.0));
        InfluencerProfile inLa = profile("Tech", "Los Angeles, CA", 20_000L, BigDecimal.valueOf(5.0));

        double ny = InfluencerSearchRanker.relevanceScore(inNy, new InfluencerSearchFilter(null, "new york", null, null, null, null));
        double la = InfluencerSearchRanker.relevanceScore(inLa, new InfluencerSearchFilter(null, "new york", null, null, null, null));

        assertThat(ny).isGreaterThan(la);
    }

    @Test
    void whenNicheMatches_tieBreakFavorsHigherEngagement() {
        InfluencerProfile highEng = profile("Fitness", "Denver", 50_000L, BigDecimal.valueOf(8.0));
        InfluencerProfile lowEng = profile("Fitness", "Denver", 50_000L, BigDecimal.valueOf(2.0));

        double hi = InfluencerSearchRanker.relevanceScore(highEng, new InfluencerSearchFilter("fitness", null, null, null, null, null));
        double lo = InfluencerSearchRanker.relevanceScore(lowEng, new InfluencerSearchFilter("fitness", null, null, null, null, null));

        assertThat(hi).isGreaterThan(lo);
    }

    @Test
    void whenFollowerRangeGiven_profileCloserToMidpoint_scoresHigher() {
        long min = 10_000L;
        long max = 50_000L;
        InfluencerProfile centered = profile("Beauty", "Miami", 30_000L, BigDecimal.valueOf(3.0));
        InfluencerProfile edge = profile("Beauty", "Miami", 10_000L, BigDecimal.valueOf(3.0));

        double edgeScore = InfluencerSearchRanker.relevanceScore(edge, new InfluencerSearchFilter("beauty", "miami", min, max, null, null));
        double centeredScore = InfluencerSearchRanker.relevanceScore(centered, new InfluencerSearchFilter("beauty", "miami", min, max, null, null));

        assertThat(centeredScore).isGreaterThan(edgeScore);
    }

    private static InfluencerProfile profile(String niche, String location, Long followers, BigDecimal engagement) {
        InfluencerProfile p = new InfluencerProfile();
        p.setNiche(niche);
        p.setLocation(location);
        p.setFollowerCount(followers);
        p.setEngagementRate(engagement);
        return p;
    }
}
