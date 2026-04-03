package com.group4.backend.controller.campaign;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.DeliverableUpdateRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.campaign.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationsController {

    private final InvitationService invitationService;
    private final CurrentUserProvider currentUserProvider;

    public CollaborationsController(InvitationService invitationService, CurrentUserProvider currentUserProvider) {
        this.invitationService = invitationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvitationResponse>> getMyCollaborations() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getCollaborationHistory(user.getId()));
    }

    /** Influencer updates deliverable status, submits content link */
    @PutMapping("/{id}/deliverable")
    public ResponseEntity<?> updateDeliverable(@PathVariable Long id, @RequestBody DeliverableUpdateRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only influencers can update deliverables"));
        }
        try {
            InvitationResponse result = invitationService.updateDeliverableStatus(id, user.getId(), request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Brand approves a submitted deliverable */
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveDeliverable(@PathVariable Long id) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only brands can approve deliverables"));
        }
        try {
            InvitationResponse result = invitationService.approveDeliverable(id, user.getId());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
