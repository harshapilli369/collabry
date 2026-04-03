package com.group4.backend.service;
import com.group4.backend.service.campaign.InvitationService;
import com.group4.backend.service.campaign.CampaignService;

import com.group4.backend.dto.DeliverableUpdateRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.dto.invitation.InvitationDetailResponse;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.dto.invitation.NegotiationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.invitation.UpdateInvitationRequest;
import com.group4.backend.model.*;
import com.group4.backend.repository.campaign.CampaignRepository;
import com.group4.backend.repository.profile.InfluencerRatingRepository;
import com.group4.backend.repository.campaign.InvitationRepository;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CampaignService campaignService;
    @Mock
    private InfluencerRatingRepository influencerRatingRepository;
    @Mock
    private BrandProfileRepository brandProfileRepository;
    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

    @InjectMocks
    private InvitationService invitationService;

    private Campaign campaign;
    private User brandUser;
    private User influencerUser;
    private CollaborationInvitation invitation;

    @BeforeEach
    void setUp() {
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setUserId(10L);
        campaign.setName("Test Campaign");
        campaign.setStatus(CampaignStatus.DRAFT);

        brandUser = new User("brand@test.com", "pass", Role.BRAND);
        brandUser.setId(10L);
        influencerUser = new User("influencer@test.com", "pass", Role.INFLUENCER);
        influencerUser.setId(20L);

        invitation = new CollaborationInvitation();
        invitation.setId(100L);
        invitation.setCampaignId(1L);
        invitation.setInfluencerId(20L);
        invitation.setBrandId(10L);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setBrandMessage("Join us");
        invitation.setCreatedAt(Instant.now());

        lenient().when(influencerRatingRepository.findByInvitationId(anyLong())).thenReturn(Optional.empty());
        lenient().when(brandProfileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        lenient().when(influencerProfileRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
    }

    @Test
    void createInvitation_shouldCreateAndReturnResponse() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Join our campaign");

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.empty());
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> {
            CollaborationInvitation inv = i.getArgument(0);
            inv.setId(100L);
            inv.setCreatedAt(Instant.now());
            return inv;
        });

        InvitationResponse response = invitationService.createInvitation(10L, 1L, request);

        assertAll(
                () -> assertThat(response).as("response not null").isNotNull(),
                () -> assertThat(response.getCampaignId()).as("campaign id").isEqualTo(1L),
                () -> assertThat(response.getInfluencerId()).as("influencer id").isEqualTo(20L),
                () -> assertThat(response.getBrandId()).as("brand id").isEqualTo(10L),
                () -> assertThat(response.getStatus()).as("status").isEqualTo(InvitationStatus.PENDING),
                () -> assertThat(response.getBrandMessage()).as("brand message").isEqualTo("Join our campaign")
        );
        verify(invitationRepository).save(any(CollaborationInvitation.class));
    }

    @Test
    void createInvitation_shouldThrowWhenCampaignNotFound() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.createInvitation(10L, 999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campaign not found");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void getInvitationsForInfluencer_returnsListOrderedByCreatedDesc() {
        when(invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForInfluencer(20L);

        assertAll(
                () -> assertThat(list).as("result size").hasSize(1),
                () -> assertThat(list.get(0).getId()).as("invitation id").isEqualTo(100L),
                () -> assertThat(list.get(0).getStatus()).as("status").isEqualTo(InvitationStatus.PENDING)
        );
    }

    @Test
    void getInvitationWithCampaignDetails_returnsInvitationAndCampaign() {
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(1L);
        campaignResponse.setName("Test Campaign");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(campaignService.findById(1L)).thenReturn(Optional.of(campaignResponse));

        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(100L, 20L);

        assertAll(
                () -> assertThat(detail).as("detail not null").isNotNull(),
                () -> assertThat(detail.getId()).as("invitation id").isEqualTo(100L),
                () -> assertThat(detail.getCampaign()).as("campaign not null").isNotNull(),
                () -> assertThat(detail.getCampaign().getName()).as("campaign name").isEqualTo("Test Campaign")
        );
    }

    @Test
    void respond_accept_shouldUpdateStatusToAccepted() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");

        InvitationResponse response = invitationService.respond(100L, 20L, request);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    void negotiate_shouldSetTermsAndStatusNegotiating() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        NegotiationRequest request = new NegotiationRequest();
        request.setProposedAmount(new BigDecimal("500.00"));
        request.setProposedTimeline("2 weeks");
        request.setProposedDeliverables("2 posts");

        InvitationResponse response = invitationService.negotiate(100L, 20L, request);

        assertAll(
                () -> assertThat(response.getStatus()).as("status").isEqualTo(InvitationStatus.NEGOTIATING),
                () -> assertThat(response.getProposedAmount()).as("proposed amount").isEqualByComparingTo("500.00")
        );
    }

    @Test
    void getCollaborationHistory_returnsOnlyAcceptedAndConfirmedForInfluencer() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findByInfluencerIdAndStatusIn(eq(20L), anyList())).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getCollaborationHistory(20L);

        assertAll(
                () -> assertThat(list).as("result size").hasSize(1),
                () -> assertThat(list.get(0).getStatus()).as("status").isEqualTo(InvitationStatus.ACCEPTED)
        );
    }

    // --- TDD: Brand sent invitations, withdraw, edit, campaign details, expired ---

    @Test
    void getInvitationsForBrand_returnsListOrderedByCreatedDesc() {
        when(invitationRepository.findByBrandIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForBrand(10L);

        assertAll(
                () -> assertThat(list).as("result size").hasSize(1),
                () -> assertThat(list.get(0).getId()).as("invitation id").isEqualTo(100L),
                () -> assertThat(list.get(0).getBrandId()).as("brand id").isEqualTo(10L)
        );
    }

    @Test
    void createInvitation_withCampaignDetails_savesDeliverablesTimelineAmountPlatformExpiresAt() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Join us");
        request.setProposedAmount(new BigDecimal("1000.00"));
        request.setProposedTimeline("2 weeks");
        request.setProposedDeliverables("2 Reels, 3 Stories");
        request.setPlatform("INSTAGRAM_REEL");
        request.setExpiresInDays(7);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.empty());
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> {
            CollaborationInvitation inv = i.getArgument(0);
            inv.setId(100L);
            inv.setCreatedAt(Instant.now());
            inv.setExpiresAt(Instant.now().plusSeconds(7 * 86400L));
            return inv;
        });

        InvitationResponse response = invitationService.createInvitation(10L, 1L, request);

        assertAll(
                () -> assertThat(response.getProposedAmount()).as("proposed amount").isEqualByComparingTo("1000.00"),
                () -> assertThat(response.getProposedTimeline()).as("proposed timeline").isEqualTo("2 weeks"),
                () -> assertThat(response.getProposedDeliverables()).as("proposed deliverables").isEqualTo("2 Reels, 3 Stories"),
                () -> assertThat(response.getPlatform()).as("platform").isEqualTo("INSTAGRAM_REEL"),
                () -> assertThat(response.getExpiresAt()).as("expires at").isNotNull()
        );
    }

    @Test
    void withdraw_asBrand_pending_setsStatusToWithdrawn() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        invitationService.withdraw(100L, 10L);

        verify(invitationRepository).save(any(CollaborationInvitation.class));
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.WITHDRAWN);
    }

    @Test
    void withdraw_wrongBrand_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.withdraw(100L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the brand that sent");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void withdraw_whenAlreadyAccepted_throws() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.withdraw(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only withdraw PENDING or NEGOTIATING");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void updateInvitation_asBrand_pending_updatesFieldsAndReturnsResponse() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        UpdateInvitationRequest request = new UpdateInvitationRequest();
        request.setMessage("Updated message");
        request.setProposedAmount(new BigDecimal("750"));
        request.setProposedTimeline("3 weeks");
        request.setPlatform("YOUTUBE_VIDEO");

        InvitationResponse response = invitationService.updateInvitation(100L, 10L, request);

        assertAll(
                () -> assertThat(response.getBrandMessage()).as("updated message").isEqualTo("Updated message"),
                () -> assertThat(response.getProposedAmount()).as("updated amount").isEqualByComparingTo("750")
        );
        verify(invitationRepository).save(invitation);
    }

    @Test
    void updateInvitation_wrongBrand_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        UpdateInvitationRequest request = new UpdateInvitationRequest();

        assertThatThrownBy(() -> invitationService.updateInvitation(100L, 99L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the brand that sent");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void getInvitationsForBrand_whenInvitationPendingButExpired_returnsEffectiveStatusExpired() {
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        when(invitationRepository.findByBrandIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForBrand(10L);

        assertAll(
                () -> assertThat(list).as("result size").hasSize(1),
                () -> assertThat(list.get(0).getStatus()).as("effective status").isEqualTo(InvitationStatus.EXPIRED)
        );
    }

    @Test
    void updateInvitation_whenAlreadyAccepted_throws() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        UpdateInvitationRequest request = new UpdateInvitationRequest();
        request.setMessage("Updated");

        assertThatThrownBy(() -> invitationService.updateInvitation(100L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only edit PENDING or NEGOTIATING invitations");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void updateInvitation_whenRejected_throws() {
        invitation.setStatus(InvitationStatus.REJECTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        UpdateInvitationRequest request = new UpdateInvitationRequest();

        assertThatThrownBy(() -> invitationService.updateInvitation(100L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only edit PENDING or NEGOTIATING invitations");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void updateInvitation_whenConfirmed_throws() {
        invitation.setStatus(InvitationStatus.CONFIRMED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        UpdateInvitationRequest request = new UpdateInvitationRequest();

        assertThatThrownBy(() -> invitationService.updateInvitation(100L, 10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only edit PENDING or NEGOTIATING invitations");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void createInvitation_whenCampaignNotOwnedByBrand_throws() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> invitationService.createInvitation(99L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("your own campaigns");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void createInvitation_whenInfluencerNotFound_throws() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(999L);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.createInvitation(10L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Influencer not found");
    }

    @Test
    void createInvitation_whenUserIsNotInfluencer_throws() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(brandUser));

        assertThatThrownBy(() -> invitationService.createInvitation(10L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User must be an influencer");
    }

    @Test
    void createInvitation_whenDuplicatePendingInvitation_throws() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        CollaborationInvitation existing = new CollaborationInvitation();
        existing.setStatus(InvitationStatus.PENDING);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> invitationService.createInvitation(10L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been invited");
    }

    @Test
    void createInvitation_defaultExpiresInDaysTo14_whenExpiresInDaysNull() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Hi");
        request.setExpiresInDays(null);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.empty());
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        invitationService.createInvitation(10L, 1L, request);

        ArgumentCaptor<CollaborationInvitation> cap = ArgumentCaptor.forClass(CollaborationInvitation.class);
        verify(invitationRepository).save(cap.capture());
        Instant exp = cap.getValue().getExpiresAt();
        assertAll(
                () -> assertThat(exp).as("expires after now").isAfter(Instant.now()),
                () -> assertThat(exp).as("expires within 15 days").isBefore(Instant.now().plusSeconds(15L * 86400L))
        );
    }

    @Test
    void createInvitation_defaultExpiresInDaysTo14_whenExpiresInDaysZero() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Hi");
        request.setExpiresInDays(0);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.empty());
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        invitationService.createInvitation(10L, 1L, request);

        ArgumentCaptor<CollaborationInvitation> cap = ArgumentCaptor.forClass(CollaborationInvitation.class);
        verify(invitationRepository).save(cap.capture());
        assertAll(
                () -> assertThat(cap.getValue().getExpiresAt()).as("expires after now").isAfter(Instant.now()),
                () -> assertThat(cap.getValue().getExpiresAt()).as("expires within 15 days").isBefore(Instant.now().plusSeconds(15L * 86400L))
        );
    }

    @Test
    void getInvitationWithCampaignDetails_whenWrongInfluencer_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.getInvitationWithCampaignDetails(100L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the invited influencer");
        verify(campaignService, never()).findById(anyLong());
    }

    @Test
    void getInvitationWithCampaignDetails_whenCampaignOptionalEmpty_leavesCampaignNull() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(campaignService.findById(1L)).thenReturn(Optional.empty());

        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(100L, 20L);

        assertThat(detail.getCampaign()).isNull();
    }

    @Test
    void respond_reject_setsRejected() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));
        RespondRequest request = new RespondRequest();
        request.setAction("reject");

        InvitationResponse response = invitationService.respond(100L, 20L, request);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.REJECTED);
    }

    @Test
    void respond_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());
        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");

        assertThatThrownBy(() -> invitationService.respond(100L, 20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void respond_whenWrongInfluencer_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.respond(100L, 99L, new RespondRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the invited influencer");
    }

    @Test
    void respond_whenExpired_throws() {
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().minusSeconds(60));
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");

        assertThatThrownBy(() -> invitationService.respond(100L, 20L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void respond_whenAlreadyAccepted_throws() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.respond(100L, 20L, new RespondRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING or NEGOTIATING");
    }

    @Test
    void negotiate_whenExpired_throws() {
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().minusSeconds(60));
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.negotiate(100L, 20L, new NegotiationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void negotiate_whenWrongInfluencer_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.negotiate(100L, 99L, new NegotiationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the invited influencer");
    }

    @Test
    void negotiate_whenStatusNotPendingOrNegotiating_throws() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.negotiate(100L, 20L, new NegotiationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDING or NEGOTIATING");
    }

    @Test
    void confirmTerms_success_setsConfirmed() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponse response = invitationService.confirmTerms(100L, 10L);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.CONFIRMED);
    }

    @Test
    void confirmTerms_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.confirmTerms(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void confirmTerms_whenWrongBrand_throws() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.confirmTerms(100L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the brand that sent");
    }

    @Test
    void confirmTerms_whenNotNegotiating_throws() {
        invitation.setStatus(InvitationStatus.PENDING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.confirmTerms(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NEGOTIATING");
    }

    @Test
    void withdraw_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.withdraw(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void withdraw_whenNegotiating_succeeds() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        invitationService.withdraw(100L, 10L);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.WITHDRAWN);
    }

    @Test
    void getInvitationsForBrand_setsRatedWhenRatingExists() {
        when(invitationRepository.findByBrandIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(invitation));
        InfluencerRating rating = new InfluencerRating();
        when(influencerRatingRepository.findByInvitationId(100L)).thenReturn(Optional.of(rating));

        List<InvitationResponse> list = invitationService.getInvitationsForBrand(10L);

        assertThat(list.get(0).getRated()).isTrue();
    }

    @Test
    void updateInvitation_withOnlyMessage_updatesMessageOnly() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));
        UpdateInvitationRequest request = new UpdateInvitationRequest();
        request.setMessage("Only msg");

        InvitationResponse response = invitationService.updateInvitation(100L, 10L, request);

        assertThat(response.getBrandMessage()).isEqualTo("Only msg");
    }

    // --- updateDeliverableStatus tests ---

    @Test
    void updateDeliverableStatus_success_updatesAllFields() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableUpdateRequest request = new DeliverableUpdateRequest();
        request.setDeliverableStatus("SUBMITTED");
        request.setContentLink("https://example.com/post");
        request.setDeliverableNotes("Draft ready");

        InvitationResponse response = invitationService.updateDeliverableStatus(100L, 20L, request);

        assertAll(
                () -> assertThat(response.getDeliverableStatus()).as("deliverable status").isEqualTo("SUBMITTED"),
                () -> assertThat(response.getContentLink()).as("content link").isEqualTo("https://example.com/post"),
                () -> assertThat(response.getDeliverableNotes()).as("deliverable notes").isEqualTo("Draft ready")
        );
    }

    @Test
    void updateDeliverableStatus_onConfirmedInvitation_succeeds() {
        invitation.setStatus(InvitationStatus.CONFIRMED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableUpdateRequest request = new DeliverableUpdateRequest();
        request.setDeliverableStatus("IN_PROGRESS");

        InvitationResponse response = invitationService.updateDeliverableStatus(100L, 20L, request);

        assertThat(response.getDeliverableStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void updateDeliverableStatus_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.updateDeliverableStatus(100L, 20L, new DeliverableUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void updateDeliverableStatus_whenWrongInfluencer_throws() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.updateDeliverableStatus(100L, 99L, new DeliverableUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the assigned influencer");
    }

    @Test
    void updateDeliverableStatus_whenStatusPending_throws() {
        invitation.setStatus(InvitationStatus.PENDING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.updateDeliverableStatus(100L, 20L, new DeliverableUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accepted or confirmed");
    }

    @Test
    void updateDeliverableStatus_withEmptyContentLink_setsNull() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableUpdateRequest request = new DeliverableUpdateRequest();
        request.setContentLink("   ");
        request.setDeliverableNotes("   ");

        InvitationResponse response = invitationService.updateDeliverableStatus(100L, 20L, request);

        assertAll(
                () -> assertThat(response.getContentLink()).as("blank content link normalized to null").isNull(),
                () -> assertThat(response.getDeliverableNotes()).as("blank notes normalized to null").isNull()
        );
    }

    @Test
    void updateDeliverableStatus_withNullFields_doesNotOverwrite() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setContentLink("existing-link");
        invitation.setDeliverableNotes("existing-notes");
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        DeliverableUpdateRequest request = new DeliverableUpdateRequest();
        // all fields null - should not overwrite

        InvitationResponse response = invitationService.updateDeliverableStatus(100L, 20L, request);

        assertAll(
                () -> assertThat(response.getContentLink()).as("content link preserved").isEqualTo("existing-link"),
                () -> assertThat(response.getDeliverableNotes()).as("notes preserved").isEqualTo("existing-notes")
        );
    }

    // --- approveDeliverable tests ---

    @Test
    void approveDeliverable_success_setsApproved() {
        invitation.setDeliverableStatus(DeliverableStatus.SUBMITTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        InvitationResponse response = invitationService.approveDeliverable(100L, 10L);

        assertThat(response.getDeliverableStatus()).isEqualTo("APPROVED");
    }

    @Test
    void approveDeliverable_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.approveDeliverable(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void approveDeliverable_whenWrongBrand_throws() {
        invitation.setDeliverableStatus(DeliverableStatus.SUBMITTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.approveDeliverable(100L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only the brand can approve");
    }

    @Test
    void approveDeliverable_whenNotSubmitted_throws() {
        invitation.setDeliverableStatus(DeliverableStatus.IN_PROGRESS);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.approveDeliverable(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only submitted deliverables");
    }

    // --- enrichment tests ---

    @Test
    void getInvitationsForInfluencer_enrichesWithBrandAndCampaignProfiles() {
        BrandProfile bp = new BrandProfile();
        bp.setUserId(10L);
        bp.setName("Test Brand");
        bp.setLogoUrl("https://logo.test/brand.png");
        bp.setIndustry("Tech");

        InfluencerProfile ip = new InfluencerProfile();
        ip.setUserId(20L);
        ip.setName("Test Influencer");
        ip.setProfilePictureUrl("https://pic.test/inf.png");
        ip.setNiche("Gaming");
        ip.setRate(new BigDecimal("500.00"));

        Campaign c = new Campaign();
        c.setId(1L);
        c.setName("Test Campaign");

        when(invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(invitation));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.of(bp));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(ip));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(c));

        List<InvitationResponse> list = invitationService.getInvitationsForInfluencer(20L);

        assertAll(
                () -> assertThat(list.get(0).getBrandName()).as("brand name").isEqualTo("Test Brand"),
                () -> assertThat(list.get(0).getBrandLogo()).as("brand logo").isEqualTo("https://logo.test/brand.png"),
                () -> assertThat(list.get(0).getBrandNiche()).as("brand niche").isEqualTo("Tech"),
                () -> assertThat(list.get(0).getInfluencerName()).as("influencer name").isEqualTo("Test Influencer"),
                () -> assertThat(list.get(0).getInfluencerProfilePicture()).as("influencer picture").isEqualTo("https://pic.test/inf.png"),
                () -> assertThat(list.get(0).getInfluencerNiche()).as("influencer niche").isEqualTo("Gaming"),
                () -> assertThat(list.get(0).getInfluencerRate()).as("influencer rate").isEqualTo("500.00"),
                () -> assertThat(list.get(0).getCampaignName()).as("campaign name").isEqualTo("Test Campaign")
        );
    }

    @Test
    void getInvitationWithCampaignDetails_enrichesWithProfiles() {
        BrandProfile bp = new BrandProfile();
        bp.setUserId(10L);
        bp.setName("Brand X");
        bp.setLogoUrl("https://logo.test/x.png");
        bp.setIndustry("Fashion");

        InfluencerProfile ip = new InfluencerProfile();
        ip.setUserId(20L);
        ip.setName("Inf Y");
        ip.setProfilePictureUrl("https://pic.test/y.png");
        ip.setNiche("Beauty");
        ip.setRate(new BigDecimal("300.00"));

        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(1L);
        campaignResponse.setName("Test Campaign");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(campaignService.findById(1L)).thenReturn(Optional.of(campaignResponse));
        when(brandProfileRepository.findByUserId(10L)).thenReturn(Optional.of(bp));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(ip));

        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(100L, 20L);

        assertAll(
                () -> assertThat(detail.getBrandName()).as("brand name").isEqualTo("Brand X"),
                () -> assertThat(detail.getInfluencerName()).as("influencer name").isEqualTo("Inf Y"),
                () -> assertThat(detail.getInfluencerRate()).as("influencer rate").isEqualTo("300.00")
        );
    }

    @Test
    void getInvitationsForInfluencer_withNullInfluencerRate_setsRateNull() {
        InfluencerProfile ip = new InfluencerProfile();
        ip.setUserId(20L);
        ip.setName("No Rate Inf");
        ip.setRate(null);

        when(invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(invitation));
        when(influencerProfileRepository.findByUserId(20L)).thenReturn(Optional.of(ip));

        List<InvitationResponse> list = invitationService.getInvitationsForInfluencer(20L);

        assertThat(list.get(0).getInfluencerRate()).isNull();
    }

    @Test
    void toResponse_whenDeliverableStatusNull_defaultsToNotStarted() {
        invitation.setDeliverableStatus(null);
        when(invitationRepository.findByInfluencerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForInfluencer(20L);

        assertThat(list.get(0).getDeliverableStatus()).isEqualTo("NOT_STARTED");
    }

    @Test
    void createInvitation_whenDuplicateNegotiatingInvitation_throws() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        CollaborationInvitation existing = new CollaborationInvitation();
        existing.setStatus(InvitationStatus.NEGOTIATING);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> invitationService.createInvitation(10L, 1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been invited");
    }

    @Test
    void createInvitation_whenPreviousInvitationRejected_allowsNew() {
        InvitationRequest request = new InvitationRequest();
        request.setInfluencerId(20L);
        request.setMessage("Try again");
        CollaborationInvitation existing = new CollaborationInvitation();
        existing.setStatus(InvitationStatus.REJECTED);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(userRepository.findById(20L)).thenReturn(Optional.of(influencerUser));
        when(invitationRepository.findByCampaignIdAndInfluencerId(1L, 20L)).thenReturn(Optional.of(existing));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> {
            CollaborationInvitation inv = i.getArgument(0);
            inv.setId(200L);
            return inv;
        });

        InvitationResponse response = invitationService.createInvitation(10L, 1L, request);

        assertThat(response.getId()).isEqualTo(200L);
    }

    @Test
    void respond_onNegotiatingInvitation_succeeds() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));
        RespondRequest request = new RespondRequest();
        request.setAction("ACCEPT");

        InvitationResponse response = invitationService.respond(100L, 20L, request);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    void updateInvitation_onNegotiatingStatus_succeeds() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));
        UpdateInvitationRequest request = new UpdateInvitationRequest();
        request.setProposedDeliverables("3 posts");

        InvitationResponse response = invitationService.updateInvitation(100L, 10L, request);

        assertThat(response.getProposedDeliverables()).isEqualTo("3 posts");
    }

    @Test
    void updateInvitation_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.updateInvitation(100L, 10L, new UpdateInvitationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void getInvitationWithCampaignDetails_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.getInvitationWithCampaignDetails(100L, 20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void negotiate_onNegotiatingStatus_succeeds() {
        invitation.setStatus(InvitationStatus.NEGOTIATING);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(CollaborationInvitation.class))).thenAnswer(i -> i.getArgument(0));

        NegotiationRequest request = new NegotiationRequest();
        request.setProposedAmount(new BigDecimal("800.00"));

        InvitationResponse response = invitationService.negotiate(100L, 20L, request);

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.NEGOTIATING);
    }

    @Test
    void negotiate_whenInvitationNotFound_throws() {
        when(invitationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.negotiate(100L, 20L, new NegotiationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invitation not found");
    }

    @Test
    void getInvitationsForBrand_withNoProfiles_returnsResponsesWithNullProfileData() {
        when(invitationRepository.findByBrandIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForBrand(10L);

        assertAll(
                () -> assertThat(list.get(0).getBrandName()).as("brand name null").isNull(),
                () -> assertThat(list.get(0).getInfluencerName()).as("influencer name null").isNull(),
                () -> assertThat(list.get(0).getCampaignName()).as("campaign name null").isNull()
        );
    }

    @Test
    void updateDeliverableStatus_whenRejected_throws() {
        invitation.setStatus(InvitationStatus.REJECTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.updateDeliverableStatus(100L, 20L, new DeliverableUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accepted or confirmed");
    }

    @Test
    void approveDeliverable_whenNotStarted_throws() {
        invitation.setDeliverableStatus(DeliverableStatus.NOT_STARTED);
        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.approveDeliverable(100L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only submitted deliverables");
    }
}
