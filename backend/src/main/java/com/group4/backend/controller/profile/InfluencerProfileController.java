package com.group4.backend.controller.profile;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.profile.CollaborationAvailabilityRequest;
import com.group4.backend.dto.profile.InfluencerProfileRequest;
import com.group4.backend.dto.profile.InfluencerProfileResponse;
import com.group4.backend.dto.profile.InfluencerSearchFilter;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.ai.GroqApiClient;
import com.group4.backend.service.profile.InfluencerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influencers")
public class InfluencerProfileController {

    private final InfluencerProfileService influencerProfileService;
    private final GroqApiClient groqApiClient;
    private final CurrentUserProvider currentUserProvider;

    public InfluencerProfileController(InfluencerProfileService influencerProfileService,
                                        GroqApiClient groqApiClient, CurrentUserProvider currentUserProvider) {
        this.influencerProfileService = influencerProfileService;
        this.groqApiClient = groqApiClient;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<InfluencerProfileResponse> getMyProfile() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return influencerProfileService.getByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<InfluencerProfileResponse> updateMyProfile(@Valid @RequestBody InfluencerProfileRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Toggle whether the influencer is open to new collaborations (persisted).
     */
    @PutMapping("/me/collaboration-availability")
    public ResponseEntity<InfluencerProfileResponse> updateCollaborationAvailability(
            @Valid @RequestBody CollaborationAvailabilityRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InfluencerProfileResponse response = influencerProfileService.updateCollaborationAvailability(
                user.getId(), Boolean.TRUE.equals(request.getOpenToCollaborations()));
        return ResponseEntity.ok(response);
    }

    /**
     * Search influencers by niche, followers, engagement rate, location. Brands only.
     * When {@code availableOnly=true}, only influencers open to collaborations are returned.
     */
    @GetMapping("/search")
    public ResponseEntity<List<InfluencerProfileResponse>> search(
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long minFollowers,
            @RequestParam(required = false) Long maxFollowers,
            @RequestParam(required = false) BigDecimal minEngagementRate,
            @RequestParam(required = false) Boolean availableOnly) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InfluencerSearchFilter filter = new InfluencerSearchFilter(
                niche, location, minFollowers, maxFollowers, minEngagementRate, availableOnly);
        List<InfluencerProfileResponse> list = influencerProfileService.search(filter);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/enhance-bio")
    public ResponseEntity<Map<String, String>> enhanceBio(@RequestBody Map<String, String> request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String bio = request.get("bio");
        if (bio == null || bio.isBlank()) throw new IllegalArgumentException("Bio text is required");
        if (!groqApiClient.isConfigured()) throw new IllegalArgumentException("AI service is not configured");
        try {
            return ResponseEntity.ok(Map.of("enhancedBio", callEnhanceBioAi(bio)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to enhance bio: " + e.getMessage()));
        }
    }

    private static final String ENHANCE_BIO_PROMPT_TEMPLATE =
            "You are a professional copywriter for influencer profiles. "
            + "Rewrite the following bio to sound more professional, engaging, and appealing to brands looking for collaborations. "
            + "Keep the same meaning and personality but make it polished. "
            + "Keep it concise (2-4 sentences max). "
            + "Return ONLY the enhanced bio text, nothing else.\n\n"
            + "Original bio:\n";

    private String callEnhanceBioAi(String bio) {
        String prompt = ENHANCE_BIO_PROMPT_TEMPLATE + bio;
        String enhanced = groqApiClient.getTextCompletion(prompt).trim();
        if (enhanced.startsWith("\"") && enhanced.endsWith("\"")) {
            enhanced = enhanced.substring(1, enhanced.length() - 1);
        }
        return enhanced;
    }
}
