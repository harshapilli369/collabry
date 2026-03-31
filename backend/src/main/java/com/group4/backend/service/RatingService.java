package com.group4.backend.service;

import com.group4.backend.dto.RatingRequest;
import com.group4.backend.dto.RatingResponse;
import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InfluencerRating;
import com.group4.backend.model.InvitationStatus;
import com.group4.backend.repository.InfluencerRatingRepository;
import com.group4.backend.repository.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatingService {

    private final InfluencerRatingRepository ratingRepository;
    private final InvitationRepository invitationRepository;

    public RatingService(InfluencerRatingRepository ratingRepository, InvitationRepository invitationRepository) {
        this.ratingRepository = ratingRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public RatingResponse submitRating(Long brandId, RatingRequest request) {
        CollaborationInvitation inv = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("Only the brand that collaborated can rate this influencer");
        }
        if (inv.getStatus() != InvitationStatus.CONFIRMED && inv.getStatus() != InvitationStatus.ACCEPTED) {
            throw new IllegalArgumentException("You can only rate after the collaboration is completed (accepted or confirmed)");
        }

        InfluencerRating rating = ratingRepository.findByInvitationId(inv.getId())
                .orElse(new InfluencerRating());
        rating.setInvitationId(inv.getId());
        rating.setBrandId(brandId);
        rating.setInfluencerId(inv.getInfluencerId());
        rating.setRating(request.getRating());
        rating.setReview(request.getReview() != null && !request.getReview().trim().isEmpty() ? request.getReview().trim() : null);
        rating = ratingRepository.save(rating);
        return toResponse(rating);
    }

    public List<RatingResponse> getRatingsForInfluencer(Long influencerId) {
        return ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public double getAverageRating(Long influencerId) {
        List<InfluencerRating> all = ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId);
        if (all.isEmpty()) return 0.0;
        return all.stream().mapToInt(InfluencerRating::getRating).average().orElse(0.0);
    }

    public List<RatingResponse> getRecentReviews(Long influencerId, int limit) {
        return ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RatingSummary getRatingSummary(Long influencerId, int recentLimit) {
        List<InfluencerRating> all = ratingRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId);
        double average = all.isEmpty() ? 0.0 : all.stream().mapToInt(InfluencerRating::getRating).average().orElse(0.0);
        int total = all.size();
        List<RatingResponse> recent = all.stream().limit(recentLimit).map(this::toResponse).collect(Collectors.toList());
        return new RatingSummary(average, total, recent);
    }

    public record RatingSummary(double averageRating, int totalRatings, List<RatingResponse> recentReviews) {}

    private RatingResponse toResponse(InfluencerRating r) {
        RatingResponse resp = new RatingResponse();
        resp.setId(r.getId());
        resp.setInvitationId(r.getInvitationId());
        resp.setBrandId(r.getBrandId());
        resp.setInfluencerId(r.getInfluencerId());
        resp.setRating(r.getRating());
        resp.setReview(r.getReview());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
