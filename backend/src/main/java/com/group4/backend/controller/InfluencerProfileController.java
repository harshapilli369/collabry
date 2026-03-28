package com.group4.backend.controller;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.InfluencerProfileService;
import com.group4.backend.service.GroqApiClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influencers")
public class InfluencerProfileController {

    private final InfluencerProfileService influencerProfileService;
    private final UserRepository userRepository;
    private final GroqApiClient groqApiClient;

    public InfluencerProfileController(InfluencerProfileService influencerProfileService, UserRepository userRepository, GroqApiClient groqApiClient) {
        this.influencerProfileService = influencerProfileService;
        this.userRepository = userRepository;
        this.groqApiClient = groqApiClient;
    }

    @GetMapping("/me")
    public ResponseEntity<InfluencerProfileResponse> getMyProfile() {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return influencerProfileService.getByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<InfluencerProfileResponse> updateMyProfile(@Valid @RequestBody InfluencerProfileRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InfluencerProfileResponse response = influencerProfileService.createOrUpdateForUser(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search influencers by niche, followers, engagement rate, location. Brands only.
     */
    @GetMapping("/search")
    public ResponseEntity<List<InfluencerProfileResponse>> search(
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long minFollowers,
            @RequestParam(required = false) Long maxFollowers,
            @RequestParam(required = false) BigDecimal minEngagementRate) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<InfluencerProfileResponse> list = influencerProfileService.search(niche, location, minFollowers, maxFollowers, minEngagementRate);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/enhance-bio")
    public ResponseEntity<Map<String, String>> enhanceBio(@RequestBody Map<String, String> request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.INFLUENCER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String bio = request.get("bio");
        if (bio == null || bio.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bio text is required"));
        }
        if (!groqApiClient.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of("message", "AI service is not configured"));
        }
        String prompt = "You are a professional copywriter for influencer profiles. " +
                "Rewrite the following bio to sound more professional, engaging, and appealing to brands looking for collaborations. " +
                "Keep the same meaning and personality but make it polished. " +
                "Keep it concise (2-4 sentences max). " +
                "Return ONLY the enhanced bio text, nothing else.\n\n" +
                "Original bio:\n" + bio;
        try {
            String enhanced = groqApiClient.getTextCompletion(prompt).trim();
            // Remove surrounding quotes if the AI wraps it
            if (enhanced.startsWith("\"") && enhanced.endsWith("\"")) {
                enhanced = enhanced.substring(1, enhanced.length() - 1);
            }
            return ResponseEntity.ok(Map.of("enhancedBio", enhanced));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to enhance bio: " + e.getMessage()));
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
