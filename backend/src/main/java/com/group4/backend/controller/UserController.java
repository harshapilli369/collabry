package com.group4.backend.controller;

import com.group4.backend.dto.InfluencerSearchResult;
import com.group4.backend.dto.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService, UserRepository userRepository) {
        super(userRepository);
        this.userService = userService;
    }

    @GetMapping("/influencers")
    public ResponseEntity<List<InfluencerSearchResult>> listInfluencers() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.listInfluencers());
    }

    @PutMapping("/me/link-social")
    public ResponseEntity<Void> linkSocialAccount(@Valid @RequestBody SocialLinkRequest request) {
        User currentUser = getCurrentUser();
        userService.linkSocialAccount(currentUser.getId(), request);
        return ResponseEntity.ok().build();
    }
}
