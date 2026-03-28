package com.group4.backend.controller;

import com.group4.backend.dto.CampaignRequest;
import com.group4.backend.dto.CampaignResponse;
import com.group4.backend.dto.InvitationRequest;
import com.group4.backend.dto.InvitationResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.CampaignService;
import com.group4.backend.service.InvitationService;
import com.group4.backend.service.AiRecommendationService;
import com.group4.backend.service.GroqApiClient;
import com.group4.backend.dto.InfluencerRecommendationDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    private final GroqApiClient groqApiClient;

    public CampaignController(CampaignService campaignService, InvitationService invitationService, UserRepository userRepository, AiRecommendationService aiRecommendationService, GroqApiClient groqApiClient) {
        this.campaignService = campaignService;
        this.invitationService = invitationService;
        this.userRepository = userRepository;
        this.aiRecommendationService = aiRecommendationService;
        this.groqApiClient = groqApiClient;
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

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(@RequestBody Map<String, String> request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String name = request.getOrDefault("name", "").trim();
        String goal = request.getOrDefault("goal", "").trim();
        String budget = request.getOrDefault("budget", "").trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Campaign name is required"));
        }
        if (!groqApiClient.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of("message", "AI service is not configured"));
        }
        String prompt = "You are a marketing copywriter for brand-influencer campaigns. " +
                "Generate a professional, compelling campaign description based on the following details. " +
                "The description should be 3-5 sentences, mention the campaign goal and target audience, " +
                "and sound appealing to influencers who might want to participate. " +
                "Return ONLY the description text, nothing else.\n\n" +
                "Campaign name: " + name + "\n" +
                (goal.isEmpty() ? "" : "Campaign goal: " + goal + "\n") +
                (budget.isEmpty() ? "" : "Budget range: " + budget + "\n");
        try {
            String description = groqApiClient.getTextCompletion(prompt).trim();
            if (description.startsWith("\"") && description.endsWith("\"")) {
                description = description.substring(1, description.length() - 1);
            }
            return ResponseEntity.ok(Map.of("description", description));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate description: " + e.getMessage()));
        }
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
