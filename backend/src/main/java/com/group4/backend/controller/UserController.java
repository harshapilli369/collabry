package com.group4.backend.controller;

import com.group4.backend.dto.profile.InfluencerSearchResult;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
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
