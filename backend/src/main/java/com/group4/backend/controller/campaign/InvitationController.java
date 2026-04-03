package com.group4.backend.controller.campaign;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.invitation.InvitationDetailResponse;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.dto.invitation.NegotiationRequest;
import com.group4.backend.dto.invitation.RespondRequest;
import com.group4.backend.dto.invitation.UpdateInvitationRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.campaign.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final CurrentUserProvider currentUserProvider;

    public InvitationController(InvitationService invitationService, CurrentUserProvider currentUserProvider) {
        this.invitationService = invitationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvitationResponse>> getMyInvitations() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForInfluencer(user.getId()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<InvitationResponse>> getSentInvitations() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForBrand(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationDetailResponse> getInvitationById(@PathVariable Long id) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(id, user.getId());
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<InvitationResponse> respond(@PathVariable Long id,
                                                       @Valid @RequestBody RespondRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.respond(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/negotiate")
    public ResponseEntity<InvitationResponse> negotiate(@PathVariable Long id,
                                                         @RequestBody NegotiationRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.negotiate(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brand/me")
    public ResponseEntity<List<InvitationResponse>> getMyInvitationsAsBrand() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForBrand(user.getId()));
    }

    @PostMapping("/{id}/confirm-terms")
    public ResponseEntity<InvitationResponse> confirmTerms(@PathVariable Long id) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.confirmTerms(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdrawInvitation(@PathVariable Long id) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        invitationService.withdraw(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvitationResponse> updateInvitation(@PathVariable Long id,
                                                                @RequestBody UpdateInvitationRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.updateInvitation(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }
}
