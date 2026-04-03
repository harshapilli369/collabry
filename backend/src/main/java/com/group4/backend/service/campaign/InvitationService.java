package com.group4.backend.service.campaign;

import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.dto.invitation.InvitationCampaignView;
import com.group4.backend.dto.invitation.InvitationDetailResponse;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.dto.invitation.NegotiationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.invitation.UpdateInvitationRequest;
import com.group4.backend.dto.DeliverableUpdateRequest;
import com.group4.backend.model.*;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.profile.InfluencerRatingRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private static final List<InvitationStatus> HISTORY_STATUSES =
            List.of(InvitationStatus.ACCEPTED, InvitationStatus.CONFIRMED);

    private static final int DEFAULT_EXPIRY_DAYS = 14;
    private static final long SECONDS_PER_DAY = 86400L;

    private final InvitationRepository invitationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignService campaignService;
    private final InfluencerRatingRepository influencerRatingRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final InfluencerProfileRepository influencerProfileRepository;

    public InvitationService(InvitationRepository invitationRepository,
                             CampaignRepository campaignRepository,
                             UserRepository userRepository,
                             CampaignService campaignService,
                             InfluencerRatingRepository influencerRatingRepository,
                             BrandProfileRepository brandProfileRepository,
                             InfluencerProfileRepository influencerProfileRepository) {
        this.invitationRepository = invitationRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.campaignService = campaignService;
        this.influencerRatingRepository = influencerRatingRepository;
        this.brandProfileRepository = brandProfileRepository;
        this.influencerProfileRepository = influencerProfileRepository;
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
        List<CollaborationInvitation> invitations = invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(influencerId);
        return enrichResponses(invitations);
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
        applyUpdateFields(inv, request);
        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    private void applyUpdateFields(CollaborationInvitation inv, UpdateInvitationRequest request) {
        if (request.getMessage() != null) inv.setBrandMessage(emptyToNull(request.getMessage()));
        if (request.getProposedAmount() != null) inv.setProposedAmount(request.getProposedAmount());
        if (request.getProposedTimeline() != null) inv.setProposedTimeline(emptyToNull(request.getProposedTimeline()));
        if (request.getProposedDeliverables() != null) inv.setProposedDeliverables(emptyToNull(request.getProposedDeliverables()));
        if (request.getPlatform() != null) inv.setPlatform(emptyToNull(request.getPlatform()));
    }

    public InvitationDetailResponse getInvitationWithCampaignDetails(Long invitationId, Long influencerId) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getInfluencerId().equals(influencerId)) {
            throw new IllegalArgumentException("You are not the invited influencer");
        }

        InvitationDetailResponse detail = toDetailResponse(inv);
        campaignService.findById(inv.getCampaignId()).ifPresent(c -> {
            detail.setCampaign(toInvitationCampaignView(c));
            detail.setCampaignName(c.getName());
        });
        enrichDetailWithProfiles(detail, inv);
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
        List<CollaborationInvitation> invitations = invitationRepository.findByInfluencerIdAndStatusIn(influencerId, HISTORY_STATUSES);
        return enrichResponses(invitations);
    }

    public List<InvitationResponse> getInvitationsForBrand(Long brandId) {
        List<CollaborationInvitation> invitations = invitationRepository.findByBrandIdOrderByCreatedAtDesc(brandId);
        List<InvitationResponse> responses = enrichResponses(invitations);
        // Mark which ones already have ratings
        for (int i = 0; i < invitations.size(); i++) {
            responses.get(i).setRated(influencerRatingRepository.findByInvitationId(invitations.get(i).getId()).isPresent());
        }
        return responses;
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
        r.setDeliverableStatus(inv.getDeliverableStatus() != null ? inv.getDeliverableStatus().name() : DeliverableStatus.NOT_STARTED.name());
        r.setContentLink(inv.getContentLink());
        r.setDeliverableNotes(inv.getDeliverableNotes());
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

    @Transactional
    public InvitationResponse updateDeliverableStatus(Long invitationId, Long influencerId, DeliverableUpdateRequest request) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getInfluencerId().equals(influencerId)) {
            throw new IllegalArgumentException("You are not the assigned influencer");
        }
        if (inv.getStatus() != InvitationStatus.ACCEPTED && inv.getStatus() != InvitationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Deliverables can only be updated on accepted or confirmed collaborations");
        }

        if (request.getDeliverableStatus() != null) {
            DeliverableStatus newStatus = DeliverableStatus.valueOf(request.getDeliverableStatus());
            inv.setDeliverableStatus(newStatus);
        }
        if (request.getContentLink() != null) {
            inv.setContentLink(request.getContentLink().trim().isEmpty() ? null : request.getContentLink().trim());
        }
        if (request.getDeliverableNotes() != null) {
            inv.setDeliverableNotes(request.getDeliverableNotes().trim().isEmpty() ? null : request.getDeliverableNotes().trim());
        }

        inv = invitationRepository.save(inv);
        return toResponse(inv);
    }

    @Transactional
    public InvitationResponse approveDeliverable(Long invitationId, Long brandId) {
        CollaborationInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!inv.getBrandId().equals(brandId)) {
            throw new IllegalArgumentException("Only the brand can approve deliverables");
        }
        if (inv.getDeliverableStatus() != DeliverableStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only submitted deliverables can be approved");
        }
        inv.setDeliverableStatus(DeliverableStatus.APPROVED);
        inv = invitationRepository.save(inv);
        return toResponse(inv);
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

    /** Batch-enrich a list of invitation responses with profile data in 2 DB queries. */
    private List<InvitationResponse> enrichResponses(List<CollaborationInvitation> invitations) {
        List<InvitationResponse> responses = invitations.stream().map(this::toResponse).collect(Collectors.toList());

        // Collect unique IDs
        var brandIds = invitations.stream().map(CollaborationInvitation::getBrandId).distinct().collect(Collectors.toList());
        var influencerIds = invitations.stream().map(CollaborationInvitation::getInfluencerId).distinct().collect(Collectors.toList());
        var campaignIds = invitations.stream().map(CollaborationInvitation::getCampaignId).distinct().collect(Collectors.toList());

        // Batch-load profiles
        Map<Long, BrandProfile> brandMap = brandIds.stream()
                .map(id -> brandProfileRepository.findByUserId(id).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(BrandProfile::getUserId, Function.identity(), (a, b) -> a));

        Map<Long, InfluencerProfile> influencerMap = influencerIds.stream()
                .map(id -> influencerProfileRepository.findByUserId(id).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toMap(InfluencerProfile::getUserId, Function.identity(), (a, b) -> a));

        Map<Long, Campaign> campaignMap = campaignIds.stream()
                .map(id -> campaignRepository.findById(id).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toMap(Campaign::getId, Function.identity(), (a, b) -> a));

        // Populate responses
        for (int i = 0; i < invitations.size(); i++) {
            CollaborationInvitation inv = invitations.get(i);
            InvitationResponse r = responses.get(i);

            BrandProfile bp = brandMap.get(inv.getBrandId());
            if (bp != null) {
                r.setBrandName(bp.getName());
                r.setBrandLogo(bp.getLogoUrl());
                r.setBrandNiche(bp.getIndustry());
            }

            InfluencerProfile ip = influencerMap.get(inv.getInfluencerId());
            if (ip != null) {
                r.setInfluencerName(ip.getName());
                r.setInfluencerProfilePicture(ip.getProfilePictureUrl());
                r.setInfluencerNiche(ip.getNiche());
                r.setInfluencerRate(ip.getRate() != null ? ip.getRate().toPlainString() : null);
            }

            Campaign c = campaignMap.get(inv.getCampaignId());
            if (c != null) {
                r.setCampaignName(c.getName());
            }
        }
        return responses;
    }

    /** Enrich a single detail response with profile data. */
    private static InvitationCampaignView toInvitationCampaignView(CampaignResponse c) {
        InvitationCampaignView v = new InvitationCampaignView();
        v.setId(c.getId());
        v.setUserId(c.getUserId());
        v.setName(c.getName());
        v.setDescription(c.getDescription());
        v.setBudgetRange(c.getBudgetRange());
        v.setStatus(c.getStatus());
        v.setCampaignGoal(c.getCampaignGoal());
        v.setPreferredContentTypes(c.getPreferredContentTypes());
        v.setStartDate(c.getStartDate());
        v.setEndDate(c.getEndDate());
        v.setNumberOfInfluencers(c.getNumberOfInfluencers());
        v.setCreatedAt(c.getCreatedAt());
        v.setUpdatedAt(c.getUpdatedAt());
        return v;
    }

    private void enrichDetailWithProfiles(InvitationDetailResponse r, CollaborationInvitation inv) {
        brandProfileRepository.findByUserId(inv.getBrandId()).ifPresent(bp -> {
            r.setBrandName(bp.getName());
            r.setBrandLogo(bp.getLogoUrl());
            r.setBrandNiche(bp.getIndustry());
        });
        influencerProfileRepository.findByUserId(inv.getInfluencerId()).ifPresent(ip -> {
            r.setInfluencerName(ip.getName());
            r.setInfluencerProfilePicture(ip.getProfilePictureUrl());
            r.setInfluencerNiche(ip.getNiche());
            r.setInfluencerRate(ip.getRate() != null ? ip.getRate().toPlainString() : null);
        });
    }
}