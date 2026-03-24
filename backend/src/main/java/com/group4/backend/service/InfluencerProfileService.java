package com.group4.backend.service;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InfluencerProfileService {

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
     *
     * @param availableOnly when {@link Boolean#TRUE}, only influencers with {@code openToCollaborations == true} are returned
     */
    public List<InfluencerProfileResponse> search(String niche, String location, Long minFollowers, Long maxFollowers,
                                                   java.math.BigDecimal minEngagementRate, Boolean availableOnly) {
        if (minFollowers != null && maxFollowers != null && minFollowers > maxFollowers) {
            throw new IllegalArgumentException("minFollowers cannot be greater than maxFollowers");
        }
        Specification<InfluencerProfile> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isTrue(root.get("isComplete")));
            if (Boolean.TRUE.equals(availableOnly)) {
                // Match entity: null means legacy row → treat as open to collaborations
                predicates.add(cb.or(
                        cb.isTrue(root.get("openToCollaborations")),
                        cb.isNull(root.get("openToCollaborations"))
                ));
            }
            if (niche != null && !niche.isBlank()) {
                String nicheTerm = "%" + niche.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("niche")), nicheTerm));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }
            if (minFollowers != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("followerCount"), minFollowers));
            }
            if (maxFollowers != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("followerCount"), maxFollowers));
            }
            if (minEngagementRate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("engagementRate"), minEngagementRate));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return influencerProfileRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
        profile.setName(emptyToNull(request.getName()) != null ? request.getName().trim() : profile.getName());
        profile.setAge(request.getAge() != null ? request.getAge() : profile.getAge());
        profile.setLocation(emptyToNull(request.getLocation()) != null ? request.getLocation().trim() : profile.getLocation());
        profile.setNiche(emptyToNull(request.getNiche()) != null ? request.getNiche().trim() : profile.getNiche());
        profile.setBio(emptyToNull(request.getBio()));
        profile.setProfilePictureUrl(emptyToNull(request.getProfilePictureUrl()));
        profile.setInstagramHandle(emptyToNull(request.getInstagramHandle()));
        profile.setYoutubeHandle(emptyToNull(request.getYoutubeHandle()));
        profile.setTiktokHandle(emptyToNull(request.getTiktokHandle()));
        profile.setRate(request.getRate());
        profile.setFollowerCount(request.getFollowerCount());
        profile.setEngagementRate(request.getEngagementRate());
        profile.setAudienceInfo(emptyToNull(request.getAudienceInfo()));

        if (request.isSaveAsDraft()) {
            profile.setComplete(false);
        } else {
            // Validate completeness: need at least one social handle and rate
            boolean hasSocialHandle = hasAny(profile.getInstagramHandle(), profile.getYoutubeHandle(), profile.getTiktokHandle());
            if (!hasSocialHandle) {
                throw new IllegalArgumentException("At least one social media handle is required to complete your profile");
            }
            if (profile.getRate() == null || profile.getRate().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Rate is required and must be zero or greater to complete your profile");
            }
            profile.setComplete(true);
        }

        profile = influencerProfileRepository.save(profile);
        return toResponse(profile);
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
        long influencerUserId = profile.getUserId();
        response.setAverageRating(ratingService.getAverageRating(influencerUserId));
        response.setTotalRatings(ratingService.getRatingsForInfluencer(influencerUserId).size());
        response.setRecentReviews(ratingService.getRecentReviews(influencerUserId, 5));
        return response;
    }
}
