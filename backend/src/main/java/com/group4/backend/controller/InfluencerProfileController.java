package com.group4.backend.controller;

import com.group4.backend.dto.InfluencerProfileRequest;
import com.group4.backend.dto.InfluencerProfileResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.InfluencerProfileService;
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

    public InfluencerProfileController(InfluencerProfileService influencerProfileService, UserRepository userRepository) {
        this.influencerProfileService = influencerProfileService;
        this.userRepository = userRepository;
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
