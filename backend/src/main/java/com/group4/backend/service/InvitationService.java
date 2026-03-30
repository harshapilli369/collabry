package com.group4.backend.service;

import com.group4.backend.dto.*;
import com.group4.backend.model.*;
import com.group4.backend.repository.CampaignRepository;
import com.group4.backend.repository.InfluencerRatingRepository;
import com.group4.backend.repository.InvitationRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final List<InvitationStatus> COLLABORATION_HISTORY_STATUSES =
            List.of(InvitationStatus.ACCEPTED, InvitationStatus.CONFIRMED);

    private static final int DEFAULT_EXPIRY_DAYS = 14;
    private static final long SECONDS_PER_DAY = 86400L;

    private final InvitationRepository invitationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignService campaignService;
    private final InfluencerRatingRepository influencerRatingRepository;

    public InvitationService(InvitationRepository invitationRepository,
                             CampaignRepository campaignRepository,
                             UserRepository userRepository,
                             CampaignService campaignService,
                             InfluencerRatingRepository influencerRatingRepository) {
        this.invitationRepository = invitationRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.campaignService = campaignService;
        this.influencerRatingRepository = influencerRatingRepository;
    }

    @Transactional
    public InvitationResponse createInvitation(Long brandId, Long campaignId, InvitationRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        if (!campaign.getUserId().equals(brandId)) {
            throw new IllegalArgumentException("You can only invite influencers to your own campaigns");
        }

        User influencer = userRepository.findById(request.getInfluencerId())
                .orElseThrow(() -> new IllegalArgumentException("Influencer not found"));
        if (influencer.getRole() != Role.INFLUENCER) {
            throw new IllegalArgumentException("User must be an influencer");
        }

        if (invitationRepository.findByCampaignIdAndInfluencerId(campaignId, request.getInfluencerId())
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING || inv.getStatus() == InvitationStatus.NEGOTIATING)
                .isPresent()) {
            throw new IllegalArgumentException("This influencer has already been invited to this campaign");
        }

        CollaborationInvitation inv = new CollaborationInvitation();
        inv.setCampaignId(campaignId);
        inv.setInfluencerId(request.getInfluencerId());
        inv.setBrandId(brandId);
        inv.setStatus(InvitationStatus.PENDING);
        inv.setBrandMessage(emptyToNull(request.getMessage()));
        inv.setProposedAmount(request.getProposedAmount());
        inv.setProposedTimeline(emptyToNull(request.getProposedTimeline()));
        inv.setProposedDeliverables(emptyToNull(request.getProposedDeliverables()));
        inv.setPlatform(emptyToNull(request.getPlatform()));
        int days = request.getExpiresInDays() != null && request.getExpiresInDays() > 0 ? request.getExpiresInDays() : DEFAULT_EXPIRY_DAYS;
        inv.setExpiresAt(java.time.Instant.now().plusSeconds(days * SECONDS_PER_DAY));

        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    public List<InvitationResponse> getInvitationsForInfluencer(Long influencerId) {
        return invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void withdraw(Long invitationId, Long brandId) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("Only the brand that sent the invitation can withdraw it");
        }
        if (inv.getStatus() != InvitationStatus.PENDING && inv.getStatus() != InvitationStatus.NEGOTIATING) {
            throw new IllegalArgumentException("You can only withdraw PENDING or NEGOTIATING invitations");
        }
        inv.setStatus(InvitationStatus.WITHDRAWN);
        invitationRepository.save(inv);
    }

    @Transactional
    public InvitationResponse updateInvitation(Long invitationId, Long brandId, UpdateInvitationRequest request) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("Only the brand that sent the invitation can edit it");
        }
        if (inv.getStatus() != InvitationStatus.PENDING && inv.getStatus() != InvitationStatus.NEGOTIATING) {
            throw new IllegalArgumentException("You can only edit PENDING or NEGOTIATING invitations");
        }
        if (request.getMessage() != null) inv.setBrandMessage(emptyToNull(request.getMessage()));
        if (request.getProposedAmount() != null) inv.setProposedAmount(request.getProposedAmount());
        if (request.getProposedTimeline() != null) inv.setProposedTimeline(emptyToNull(request.getProposedTimeline()));
        if (request.getProposedDeliverables() != null) inv.setProposedDeliverables(emptyToNull(request.getProposedDeliverables()));
        if (request.getPlatform() != null) inv.setPlatform(emptyToNull(request.getPlatform()));
        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    public InvitationDetailResponse getInvitationWithCampaignDetails(Long invitationId, Long influencerId) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getInfluencerId().equals(influencerId)) {
            throw new IllegalArgumentException("You are not the invited influencer");
        }

        InvitationDetailResponse detail = toDetailResponse(inv);
        campaignService.findById(inv.getCampaignId()).ifPresent(detail::setCampaign);
        return detail;
    }

    @Transactional
    public InvitationResponse respond(Long invitationId, Long influencerId, RespondRequest request) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getInfluencerId().equals(influencerId)) {
            throw new IllegalArgumentException("You are not the invited influencer");
        }
        if (effectiveStatus(inv) == InvitationStatus.EXPIRED) {
            throw new IllegalArgumentException("This invitation has expired");
        }
        if (inv.getStatus() != InvitationStatus.PENDING && inv.getStatus() != InvitationStatus.NEGOTIATING) {
            throw new IllegalArgumentException("You can only respond to PENDING or NEGOTIATING invitations");
        }

        InvitationStatus newStatus = "ACCEPT".equalsIgnoreCase(request.getAction())
                ? InvitationStatus.ACCEPTED
                : InvitationStatus.REJECTED;
        inv.setStatus(newStatus);
        inv.setRespondedAt(java.time.Instant.now());
        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    @Transactional
    public InvitationResponse negotiate(Long invitationId, Long influencerId, NegotiationRequest request) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getInfluencerId().equals(influencerId)) {
            throw new IllegalArgumentException("You are not the invited influencer");
        }
        if (effectiveStatus(inv) == InvitationStatus.EXPIRED) {
            throw new IllegalArgumentException("This invitation has expired");
        }
        if (inv.getStatus() != InvitationStatus.PENDING && inv.getStatus() != InvitationStatus.NEGOTIATING) {
            throw new IllegalArgumentException("You can only negotiate on PENDING or NEGOTIATING invitations");
        }

        inv.setStatus(InvitationStatus.NEGOTIATING);
        inv.setProposedAmount(request.getProposedAmount());
        inv.setProposedTimeline(emptyToNull(request.getProposedTimeline()));
        inv.setProposedDeliverables(emptyToNull(request.getProposedDeliverables()));
        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    public List<InvitationResponse> getCollaborationHistory(Long influencerId) {
        return invitationRepository.findByInfluencerIdAndStatusIn(influencerId, COLLABORATION_HISTORY_STATUSES)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InvitationResponse> getInvitationsForBrand(Long brandId) {
        return invitationRepository.findByBrandIdOrderByCreatedAtDesc(brandId)
                .stream()
                .map(inv -> {
                    InvitationResponse r = toResponse(inv);
                    r.setRated(influencerRatingRepository.findByInvitationId(inv.getId()).isPresent());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public InvitationResponse confirmTerms(Long invitationId, Long brandId) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("Only the brand that sent the invitation can confirm terms");
        }
        if (inv.getStatus() != InvitationStatus.NEGOTIATING) {
            throw new IllegalArgumentException("Terms can only be confirmed when invitation is in NEGOTIATING status");
        }

        inv.setStatus(InvitationStatus.CONFIRMED);
        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    private static String emptyToNull(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
    }

    private InvitationResponse toResponse(CollaborationInvitation inv) {
        InvitationResponse r = new InvitationResponse();
        r.setId(inv.getId());
        r.setCampaignId(inv.getCampaignId());
        r.setInfluencerId(inv.getInfluencerId());
        r.setBrandId(inv.getBrandId());
        r.setStatus(effectiveStatus(inv));
        r.setBrandMessage(inv.getBrandMessage());
        r.setProposedAmount(inv.getProposedAmount());
        r.setProposedTimeline(inv.getProposedTimeline());
        r.setProposedDeliverables(inv.getProposedDeliverables());
        r.setPlatform(inv.getPlatform());
        r.setExpiresAt(inv.getExpiresAt());
        r.setCreatedAt(inv.getCreatedAt());
        r.setUpdatedAt(inv.getUpdatedAt());
        r.setRespondedAt(inv.getRespondedAt());
        return r;
    }

    private InvitationStatus effectiveStatus(CollaborationInvitation inv) {
        if (isPendingAndExpired(inv)) {
            return InvitationStatus.EXPIRED;
        }
        return inv.getStatus();
    }

    private static boolean isPendingAndExpired(CollaborationInvitation inv) {
        return inv.getStatus() == InvitationStatus.PENDING
                && inv.getExpiresAt() != null
                && java.time.Instant.now().isAfter(inv.getExpiresAt());
    }

    private InvitationDetailResponse toDetailResponse(CollaborationInvitation inv) {
        InvitationResponse base = toResponse(inv);
        InvitationDetailResponse r = new InvitationDetailResponse();
        r.setId(base.getId());
        r.setCampaignId(base.getCampaignId());
        r.setInfluencerId(base.getInfluencerId());
        r.setBrandId(base.getBrandId());
        r.setStatus(base.getStatus());
        r.setBrandMessage(base.getBrandMessage());
        r.setProposedAmount(base.getProposedAmount());
        r.setProposedTimeline(base.getProposedTimeline());
        r.setProposedDeliverables(base.getProposedDeliverables());
        r.setPlatform(base.getPlatform());
        r.setExpiresAt(base.getExpiresAt());
        r.setCreatedAt(base.getCreatedAt());
        r.setUpdatedAt(base.getUpdatedAt());
        r.setRespondedAt(base.getRespondedAt());
        return r;
    }
}