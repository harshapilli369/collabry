package com.group4.backend.service;

import com.group4.backend.dto.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class InvitationService {

    public InvitationResponse createInvitation(Long brandId, Long campaignId, InvitationRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    public List<InvitationResponse> getInvitationsForInfluencer(Long influencerId) {
        throw new UnsupportedOperationException("not implemented");
    }

    public InvitationDetailResponse getInvitationWithCampaignDetails(Long invitationId, Long influencerId) {
        throw new UnsupportedOperationException("not implemented");
    }

    public InvitationResponse respond(Long invitationId, Long influencerId, RespondRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    public InvitationResponse negotiate(Long invitationId, Long influencerId, NegotiationRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    public List<InvitationResponse> getCollaborationHistory(Long influencerId) {
        throw new UnsupportedOperationException("not implemented");
    }

    public InvitationResponse confirmTerms(Long invitationId, Long brandId) {
        throw new UnsupportedOperationException("not implemented");
    }
}