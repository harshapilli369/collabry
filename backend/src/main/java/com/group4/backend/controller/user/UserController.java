package com.group4.backend.controller.user;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.VerificationStatusResponse;
import com.group4.backend.dto.profile.InfluencerSearchResult;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.user.VerificationService;
import com.group4.backend.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final VerificationService verificationService;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider, VerificationService verificationService) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
        this.verificationService = verificationService;
    }

    @PostMapping("/me/verification/request")
    public ResponseEntity<VerificationStatusResponse> requestVerification() {
        User user = currentUserProvider.getCurrentUser();
        logger.info("User {} (ID: {}) requesting verification", user.getEmail(), user.getId());
        return ResponseEntity.ok(verificationService.createRequest(user.getId()));
    }

    @GetMapping("/me/verification/status")
    public ResponseEntity<VerificationStatusResponse> getVerificationStatus() {
        User user = currentUserProvider.getCurrentUser();
        return verificationService.getLatestRequest(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/influencers")
    public ResponseEntity<List<InfluencerSearchResult>> listInfluencers() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.listInfluencers());
    }

    @PutMapping("/me/link-social")
    public ResponseEntity<Void> linkSocialAccount(@Valid @RequestBody SocialLinkRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        userService.linkSocialAccount(currentUser.getId(), request);
        return ResponseEntity.ok().build();
    }
}
