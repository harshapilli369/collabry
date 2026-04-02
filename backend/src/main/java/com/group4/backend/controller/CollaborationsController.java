package com.group4.backend.controller;

import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.campaign.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
