package com.group4.backend.controller.profile;

import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.rating.RatingRequest;
import com.group4.backend.dto.rating.RatingResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.service.profile.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final CurrentUserProvider currentUserProvider;

    public RatingController(RatingService ratingService, CurrentUserProvider currentUserProvider) {
        this.ratingService = ratingService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(@Valid @RequestBody RatingRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        RatingResponse response = ratingService.submitRating(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
