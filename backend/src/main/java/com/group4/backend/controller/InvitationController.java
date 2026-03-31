package com.group4.backend.controller;

import com.group4.backend.dto.*;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController extends BaseController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService, UserRepository userRepository) {
        super(userRepository);
        this.invitationService = invitationService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvitationResponse>> getMyInvitations() {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForInfluencer(user.getId()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<InvitationResponse>> getSentInvitations() {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForBrand(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationDetailResponse> getInvitationById(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationDetailResponse detail = invitationService.getInvitationWithCampaignDetails(id, user.getId());
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<InvitationResponse> respond(@PathVariable Long id,
                                                       @Valid @RequestBody RespondRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.respond(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/negotiate")
    public ResponseEntity<InvitationResponse> negotiate(@PathVariable Long id,
                                                         @RequestBody NegotiationRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.negotiate(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brand/me")
    public ResponseEntity<List<InvitationResponse>> getMyInvitationsAsBrand() {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getInvitationsForBrand(user.getId()));
    }

    @PostMapping("/{id}/confirm-terms")
    public ResponseEntity<InvitationResponse> confirmTerms(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.confirmTerms(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdrawInvitation(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        invitationService.withdraw(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvitationResponse> updateInvitation(@PathVariable Long id,
                                                                @RequestBody UpdateInvitationRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.updateInvitation(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }
}
