package com.group4.backend.service.profile;

import com.group4.backend.dto.profile.InfluencerProfileRequest;
import com.group4.backend.dto.profile.InfluencerProfileResponse;
import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InfluencerProfileService {

    private static final int RECENT_REVIEWS_LIMIT = 5;

    private final InfluencerProfileRepository influencerProfileRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;

    public InfluencerProfileService(InfluencerProfileRepository influencerProfileRepository,
                                   UserRepository userRepository,
                                   RatingService ratingService) {
        this.influencerProfileRepository = influencerProfileRepository;
        this.userRepository = userRepository;
        this.ratingService = ratingService;
    }

    public Optional<InfluencerProfileResponse> getByUserId(Long userId) {
        return influencerProfileRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    /**
     * Search discoverable (complete) influencer profiles by niche, location, followers, engagement rate.
     * For use by brands to find influencers.
     * <p>
     * Rows are indexed on niche, follower_count, engagement_rate, and is_complete for efficient filtering.
     * Results are ordered by {@linkplain InfluencerSearchRanker relevance score} (niche match quality, location match,
     * engagement/follower signals, and fit to follower range), then by {@code createdAt} descending.
     *
     * @param availableOnly when {@link Boolean#TRUE}, only influencers with {@code openToCollaborations == true} are returned
     */
    public List<InfluencerProfileResponse> search(InfluencerSearchFilter filter) {
        if (isInvalidFollowerRange(filter.minFollowers(), filter.maxFollowers())) {
            throw new IllegalArgumentException("minFollowers cannot be greater than maxFollowers");
        }
        Specification<InfluencerProfile> spec = buildSearchSpec(filter);
        List<InfluencerProfile> profiles = new ArrayList<>(influencerProfileRepository.findAll(spec));
        profiles.sort(Comparator
                .comparingDouble((InfluencerProfile p) ->
                        -InfluencerSearchRanker.relevanceScore(p, filter))
                .thenComparing(InfluencerProfile::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return profiles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Specification<InfluencerProfile> buildSearchSpec(InfluencerSearchFilter filter) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isTrue(root.get("isComplete")));
            if (Boolean.TRUE.equals(filter.availableOnly())) {
                predicates.add(cb.or(
                        cb.isTrue(root.get("openToCollaborations")),
                        cb.isNull(root.get("openToCollaborations"))
                ));
            }
            if (filter.niche() != null && !filter.niche().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("niche")), "%" + filter.niche().trim().toLowerCase() + "%"));
            }
            if (filter.location() != null && !filter.location().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filter.location().trim().toLowerCase() + "%"));
            }
            if (filter.minFollowers() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("followerCount"), filter.minFollowers()));
            }
            if (filter.maxFollowers() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("followerCount"), filter.maxFollowers()));
            }
            if (filter.minEngagementRate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("engagementRate"), filter.minEngagementRate()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Transactional
    public InfluencerProfileResponse createOrUpdateForUser(Long userId, InfluencerProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.INFLUENCER) {
            throw new IllegalArgumentException("Only influencer users can create or update an influencer profile");
        }

        InfluencerProfile profile = influencerProfileRepository.findByUserId(userId)
                .orElseGet(InfluencerProfile::new);
        profile.setUserId(userId);
        applyFieldsFromRequest(profile, request);
        updateCompletionStatus(profile, request.isSaveAsDraft());

        profile = influencerProfileRepository.save(profile);
        return toResponse(profile);
    }

    private void applyFieldsFromRequest(InfluencerProfile profile, InfluencerProfileRequest request) {
        if (emptyToNull(request.getName()) != null) profile.setName(request.getName().trim());
        if (request.getAge() != null) profile.setAge(request.getAge());
        if (emptyToNull(request.getLocation()) != null) profile.setLocation(request.getLocation().trim());
        if (emptyToNull(request.getNiche()) != null) profile.setNiche(request.getNiche().trim());
        profile.setBio(emptyToNull(request.getBio()));
        profile.setProfilePictureUrl(emptyToNull(request.getProfilePictureUrl()));
        profile.setInstagramHandle(emptyToNull(request.getInstagramHandle()));
        profile.setYoutubeHandle(emptyToNull(request.getYoutubeHandle()));
        profile.setTiktokHandle(emptyToNull(request.getTiktokHandle()));
        profile.setRate(request.getRate());
        profile.setFollowerCount(request.getFollowerCount());
        profile.setEngagementRate(request.getEngagementRate());
        profile.setAudienceInfo(emptyToNull(request.getAudienceInfo()));
    }

    private void updateCompletionStatus(InfluencerProfile profile, boolean saveAsDraft) {
        if (saveAsDraft) {
            profile.setComplete(false);
            return;
        }
        if (!hasAny(profile.getInstagramHandle(), profile.getYoutubeHandle(), profile.getTiktokHandle())) {
            throw new IllegalArgumentException("At least one social media handle is required to complete your profile");
        }
        if (profile.getRate() == null || profile.getRate().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rate is required and must be zero or greater to complete your profile");
        }
        profile.setComplete(true);
    }

    @Transactional
    public InfluencerProfileResponse updateCollaborationAvailability(Long userId, boolean openToCollaborations) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.INFLUENCER) {
            throw new IllegalArgumentException("Only influencer users can update collaboration availability");
        }
        InfluencerProfile profile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Influencer profile not found"));
        profile.setOpenToCollaborations(openToCollaborations);
        profile = influencerProfileRepository.save(profile);
        return toResponse(profile);
    }

    private static boolean isInvalidFollowerRange(Long minFollowers, Long maxFollowers) {
        return minFollowers != null && maxFollowers != null && minFollowers > maxFollowers;
    }

    private static boolean hasAny(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return true;
        }
        return false;
    }

    private static String emptyToNull(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private InfluencerProfileResponse toResponse(InfluencerProfile profile) {
        InfluencerProfileResponse response = new InfluencerProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setName(profile.getName());
        response.setAge(profile.getAge());
        response.setLocation(profile.getLocation());
        response.setNiche(profile.getNiche());
        response.setBio(profile.getBio());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());
        response.setInstagramHandle(profile.getInstagramHandle());
        response.setYoutubeHandle(profile.getYoutubeHandle());
        response.setTiktokHandle(profile.getTiktokHandle());
        response.setRate(profile.getRate());
        response.setFollowerCount(profile.getFollowerCount());
        response.setEngagementRate(profile.getEngagementRate());
        response.setAudienceInfo(profile.getAudienceInfo());
        response.setComplete(profile.isComplete());
        response.setOpenToCollaborations(profile.isOpenToCollaborations());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        response.setVerified(profile.isVerified());
        long influencerUserId = profile.getUserId();
        RatingService.RatingSummary ratingSummary = ratingService.getRatingSummary(influencerUserId, RECENT_REVIEWS_LIMIT);
        response.setAverageRating(ratingSummary.averageRating());
        response.setTotalRatings(ratingSummary.totalRatings());
        response.setRecentReviews(ratingSummary.recentReviews());
        return response;
    }
}
