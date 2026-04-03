package com.group4.backend.controller.campaign;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.campaign.CampaignRequest;
import com.group4.backend.dto.campaign.CampaignResponse;
import com.group4.backend.dto.invitation.InvitationRequest;
import com.group4.backend.dto.invitation.InvitationResponse;
import com.group4.backend.dto.profile.InfluencerRecommendationDTO;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.ai.AiRecommendationService;
import com.group4.backend.service.campaign.CampaignService;
import com.group4.backend.service.campaign.CampaignReportService;
import com.group4.backend.service.ai.GroqApiClient;
import com.group4.backend.service.campaign.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    /** Test user allowed to create campaigns without verification. */
    private static final String TEST_BRAND_EMAIL = "brand@collabry";

    private final CampaignService campaignService;
    private final InvitationService invitationService;
    private final AiRecommendationService aiRecommendationService;
    private final GroqApiClient groqApiClient;
    private final CampaignReportService campaignReportService;
    private final CurrentUserProvider currentUserProvider;

    public CampaignController(CampaignService campaignService, InvitationService invitationService,
                               AiRecommendationService aiRecommendationService,
                               GroqApiClient groqApiClient, CampaignReportService campaignReportService,
                               CurrentUserProvider currentUserProvider) {
        this.campaignService = campaignService;
        this.invitationService = invitationService;
        this.aiRecommendationService = aiRecommendationService;
        this.groqApiClient = groqApiClient;
        this.campaignReportService = campaignReportService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        CampaignResponse response = campaignService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<CampaignResponse>> getMyCampaigns() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(campaignService.findByUserId(user.getId()));
    }
    
    @PatchMapping("/{campaignId}/status")
    public ResponseEntity<CampaignResponse> updateStatus(@PathVariable Long campaignId,
                                                         @RequestBody Map<String, String> request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String statusStr = request.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("Status is required");
        }
        com.group4.backend.model.CampaignStatus newStatus = com.group4.backend.model.CampaignStatus.valueOf(statusStr.toUpperCase());
        CampaignResponse response = campaignService.updateStatus(user.getId(), campaignId, newStatus);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{campaignId}/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(@PathVariable Long campaignId,
                                                                @Valid @RequestBody InvitationRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InvitationResponse response = invitationService.createInvitation(user.getId(), campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{campaignId}/recommendations")
    public ResponseEntity<List<InfluencerRecommendationDTO>> getRecommendations(@PathVariable Long campaignId) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<InfluencerRecommendationDTO> recommendations = aiRecommendationService.getRecommendations(campaignId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/generate-description")
    public ResponseEntity<Map<String, String>> generateDescription(@RequestBody Map<String, String> request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String name = request.getOrDefault("name", "").trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Campaign name is required");
        if (!groqApiClient.isConfigured()) throw new IllegalArgumentException("AI service is not configured");
        try {
            String goal = request.getOrDefault("goal", "").trim();
            String budget = request.getOrDefault("budget", "").trim();
            return ResponseEntity.ok(Map.of("description", callDescriptionAi(name, goal, budget)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to generate description: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/{campaignId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCampaignReport(@PathVariable Long campaignId) {
        User user = currentUserProvider.getCurrentUser();
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

    private static final String DESCRIPTION_PROMPT_PREFIX =
            "You are a marketing copywriter for brand-influencer campaigns. "
            + "Generate a professional, compelling campaign description based on the following details. "
            + "The description should be 3-5 sentences, mention the campaign goal and target audience, "
            + "and sound appealing to influencers who might want to participate. "
            + "Return ONLY the description text, nothing else.\n\n";

    private String callDescriptionAi(String name, String goal, String budget) {
        String prompt = DESCRIPTION_PROMPT_PREFIX
                + "Campaign name: " + name + "\n"
                + (goal.isEmpty() ? "" : "Campaign goal: " + goal + "\n")
                + (budget.isEmpty() ? "" : "Budget range: " + budget + "\n");
        String description = groqApiClient.getTextCompletion(prompt).trim();
        if (description.startsWith("\"") && description.endsWith("\"")) {
            description = description.substring(1, description.length() - 1);
        }
        return description;
    }


}
