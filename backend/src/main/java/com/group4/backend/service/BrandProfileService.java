package com.group4.backend.service;

import com.group4.backend.dto.BrandProfileRequest;
import com.group4.backend.dto.BrandProfileResponse;
import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.BrandProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BrandProfileService {

    private final BrandProfileRepository brandProfileRepository;
    private final UserRepository userRepository;

    public BrandProfileService(BrandProfileRepository brandProfileRepository, UserRepository userRepository) {
        this.brandProfileRepository = brandProfileRepository;
        this.userRepository = userRepository;
    }

    public Optional<BrandProfileResponse> getByUserId(Long userId) {
        return brandProfileRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    @Transactional
    public BrandProfileResponse createOrUpdateForUser(Long userId, BrandProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.BRAND) {
            throw new IllegalArgumentException("Only brand users can create or update a brand profile");
        }

        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseGet(BrandProfile::new);

        profile.setUserId(userId);
        profile.setName(request.getName());
        profile.setIndustry(request.getIndustry());
        profile.setWebsite(request.getWebsite());
        profile.setEmail(request.getEmail());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setDescription(request.getDescription());
        profile.setInstagramUrl(request.getInstagramUrl());
        profile.setLinkedInUrl(request.getLinkedInUrl());
        profile.setTwitterUrl(request.getTwitterUrl());
        profile.setBudgetRange(request.getBudgetRange());

        profile = brandProfileRepository.save(profile);
        return toResponse(profile);
    }

    public Optional<BrandProfileResponse> getPublicProfile(Long brandUserId) {
        return brandProfileRepository.findByUserId(brandUserId)
                .map(this::toResponse);
    }

    private BrandProfileResponse toResponse(BrandProfile profile) {
        BrandProfileResponse response = new BrandProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setName(profile.getName());
        response.setIndustry(profile.getIndustry());
        response.setWebsite(profile.getWebsite());
        response.setEmail(profile.getEmail());
        response.setLogoUrl(profile.getLogoUrl());
        response.setDescription(profile.getDescription());
        response.setInstagramUrl(profile.getInstagramUrl());
        response.setLinkedInUrl(profile.getLinkedInUrl());
        response.setTwitterUrl(profile.getTwitterUrl());
        response.setBudgetRange(profile.getBudgetRange());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
