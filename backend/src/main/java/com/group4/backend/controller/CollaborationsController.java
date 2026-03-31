package com.group4.backend.controller;

import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationsController extends BaseController {

    private final InvitationService invitationService;

    public CollaborationsController(InvitationService invitationService, UserRepository userRepository) {
        super(userRepository);
        this.invitationService = invitationService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvitationResponse>> getMyCollaborations() {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getCollaborationHistory(user.getId()));
    }
}
