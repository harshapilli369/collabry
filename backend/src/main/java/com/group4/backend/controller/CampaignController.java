package com.group4.backend.controller;

import com.group4.backend.dto.CampaignRequest;
import com.group4.backend.dto.CampaignResponse;
import com.group4.backend.dto.InvitationRequest;
import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.CampaignService;
import com.group4.backend.service.CampaignReportService;
import com.group4.backend.service.InvitationService;
import com.group4.backend.service.AiRecommendationService;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    /** Test user allowed to create campaigns without verification. */
    private static final String TEST_BRAND_EMAIL = "brand@collabry";

    private final CampaignService campaignService;
    private final InvitationService invitationService;
    private final UserRepository userRepository;
    private final AiRecommendationService aiRecommendationService;
    private final CampaignReportService campaignReportService;

    public CampaignController(CampaignService campaignService, InvitationService invitationService, UserRepository userRepository,
                              AiRecommendationService aiRecommendationService, CampaignReportService campaignReportService) {
        this.campaignService = campaignService;
        this.invitationService = invitationService;
        this.userRepository = userRepository;
        this.aiRecommendationService = aiRecommendationService;
        this.campaignReportService = campaignReportService;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isAllowedToCreateCampaigns(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CampaignResponse response = campaignService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns() {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(campaignService.findByUserId(user.getId()));
    }

    @PostMapping("/{campaignId}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(@PathVariable Long campaignId, @Valid @RequestBody InvitationRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isAllowedToCreateCampaigns(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.createInvitation(user.getId(), campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{campaignId}/recommendations")
    public ResponseEntity<List<InfluencerRecommendationDTO>> getRecommendations(@PathVariable Long campaignId) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isAllowedToCreateCampaigns(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<InfluencerRecommendationDTO> recommendations = aiRecommendationService.getRecommendations(campaignId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping(value = "/{campaignId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCampaignReport(@PathVariable Long campaignId) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        byte[] pdf = campaignReportService.generateCampaignReportPdf(user.getId(), campaignId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"campaign-" + campaignId + "-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    private boolean isAllowedToCreateCampaigns(User user) {
        return user.isVerified() || TEST_BRAND_EMAIL.equalsIgnoreCase(user.getEmail());
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
