package com.group4.backend.controller;

import com.group4.backend.dto.RatingRequest;
import com.group4.backend.dto.RatingResponse;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController extends BaseController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService, UserRepository userRepository) {
        super(userRepository);
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(@Valid @RequestBody RatingRequest request) {
        User user = getCurrentUser();
        if (user.getRole() != Role.BRAND) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        RatingResponse response = ratingService.submitRating(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
