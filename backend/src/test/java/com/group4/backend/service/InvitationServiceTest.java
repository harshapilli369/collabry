package com.group4.backend.service;

import com.group4.backend.dto.*;
import com.group4.backend.model.*;
import com.group4.backend.repository.CampaignRepository;
import com.group4.backend.repository.InfluencerRatingRepository;
import com.group4.backend.repository.InvitationRepository;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        assertThat(response).isNotNull();
        assertThat(response.getCampaignId()).isEqualTo(1L);
        assertThat(response.getInfluencerId()).isEqualTo(20L);
        assertThat(response.getBrandId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(response.getBrandMessage()).isEqualTo("Join our campaign");
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

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(100L);
        assertThat(list.get(0).getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void getInvitationWithCampaignDetails_returnsInvitationAndCampaign() {
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(1L);
        campaignResponse.setName("Test Campaign");

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(campaignService.findById(1L)).thenReturn(Optional.of(campaignResponse));

        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(100L, 20L);

        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(100L);
        assertThat(detail.getCampaign()).isNotNull();
        assertThat(detail.getCampaign().getName()).isEqualTo("Test Campaign");
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

        assertThat(response.getStatus()).isEqualTo(InvitationStatus.NEGOTIATING);
        assertThat(response.getProposedAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void getCollaborationHistory_returnsOnlyAcceptedAndConfirmedForInfluencer() {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findByInfluencerIdAndStatusIn(eq(20L), anyList())).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getCollaborationHistory(20L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    // --- TDD: Brand sent invitations, withdraw, edit, campaign details, expired ---

    @Test
    void getInvitationsForBrand_returnsListOrderedByCreatedDesc() {
        when(invitationRepository.findByBrandIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(invitation));

        List<InvitationResponse> list = invitationService.getInvitationsForBrand(10L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(100L);
        assertThat(list.get(0).getBrandId()).isEqualTo(10L);
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

        assertThat(response.getProposedAmount()).isEqualByComparingTo("1000.00");
        assertThat(response.getProposedTimeline()).isEqualTo("2 weeks");
        assertThat(response.getProposedDeliverables()).isEqualTo("2 Reels, 3 Stories");
        assertThat(response.getPlatform()).isEqualTo("INSTAGRAM_REEL");
        assertThat(response.getExpiresAt()).isNotNull();
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

        assertThat(response.getBrandMessage()).isEqualTo("Updated message");
        assertThat(response.getProposedAmount()).isEqualByComparingTo("750");
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

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo(InvitationStatus.EXPIRED);
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
}
