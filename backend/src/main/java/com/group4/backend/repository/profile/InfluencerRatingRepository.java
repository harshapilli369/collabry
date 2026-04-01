package com.group4.backend.repository.profile;

import com.group4.backend.model.InfluencerRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InfluencerRatingRepository extends JpaRepository<InfluencerRating, Long> {

    List<InfluencerRating> findByInfluencerIdOrderByCreatedAtDesc(Long influencerId);

    Optional<InfluencerRating> findByInvitationId(Long invitationId);
}
