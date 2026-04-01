package com.group4.backend.repository.campaign;

import com.group4.backend.model.CollaborationInvitation;
import com.group4.backend.model.InvitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<CollaborationInvitation, Long> {

    List<CollaborationInvitation> findByInfluencerIdOrderByCreatedAtDesc(Long influencerId);

    List<CollaborationInvitation> findByBrandIdOrderByCreatedAtDesc(Long brandId);

    Optional<CollaborationInvitation> findByCampaignIdAndInfluencerId(Long campaignId, Long influencerId);

    List<CollaborationInvitation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<CollaborationInvitation> findByInfluencerIdAndStatusIn(Long influencerId, List<InvitationStatus> statuses);

    List<CollaborationInvitation> findByBrandIdAndStatus(Long brandId, InvitationStatus status);

    Page<CollaborationInvitation> findByStatusOrderByUpdatedAtDesc(InvitationStatus status, Pageable pageable);
}
