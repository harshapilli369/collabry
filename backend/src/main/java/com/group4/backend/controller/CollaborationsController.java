package com.group4.backend.controller;

import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationsController {

    private final InvitationService invitationService;
    private final UserRepository userRepository;

    public CollaborationsController(InvitationService invitationService, UserRepository userRepository) {
        this.invitationService = invitationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<List<InvitationResponse>> getMyCollaborations() {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invitationService.getCollaborationHistory(user.getId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
