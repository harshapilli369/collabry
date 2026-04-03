package com.group4.backend.controller.profile;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.dto.profile.BrandProfileResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.profile.BrandProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
public class BrandProfileController {

    private final BrandProfileService brandProfileService;
    private final CurrentUserProvider currentUserProvider;

    public BrandProfileController(BrandProfileService brandProfileService, CurrentUserProvider currentUserProvider) {
        this.brandProfileService = brandProfileService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public ResponseEntity<BrandProfileResponse> getMyProfile() {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return brandProfileService.getByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<BrandProfileResponse> updateMyProfile(@Valid @RequestBody BrandProfileRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        BrandProfileResponse response = brandProfileService.createOrUpdateForUser(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{brandId}/profile")
    public ResponseEntity<BrandProfileResponse> getBrandProfile(@PathVariable Long brandId) {
        return brandProfileService.getPublicProfile(brandId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
