package com.group4.backend.service;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        response.setAudienceInfo(profile.getAudienceInfo());
        response.setComplete(profile.isComplete());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
