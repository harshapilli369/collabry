package com.group4.backend.service.profile;

import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.dto.profile.BrandProfileResponse;
import com.group4.backend.model.BrandProfile;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.user.UserRepository;
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
        profile.setName(request.getName().trim());
        profile.setIndustry(request.getIndustry().trim());
        profile.setWebsite(normalizeWebsite(request.getWebsite()));
        profile.setEmail(request.getEmail().trim());
        profile.setLogoUrl(emptyToNull(request.getLogoUrl()));
        profile.setDescription(emptyToNull(request.getDescription()));
        profile.setInstagramUrl(emptyToNull(request.getInstagramUrl()));
        profile.setLinkedInUrl(emptyToNull(request.getLinkedInUrl()));
        profile.setTwitterUrl(emptyToNull(request.getTwitterUrl()));
        profile.setBudgetRange(request.getBudgetRange());

        profile = brandProfileRepository.save(profile);
        return toResponse(profile);
    }

    public Optional<BrandProfileResponse> getPublicProfile(Long brandUserId) {
        return brandProfileRepository.findByUserId(brandUserId)
                .map(this::toResponse);
    }

    private static String normalizeWebsite(String website) {
        if (website == null) return null;
        String s = website.trim();
        if (s.isEmpty()) return s;
        if (hasKnownScheme(s.toLowerCase())) return s;
        return "https://" + s;
    }

    private static boolean hasKnownScheme(String lower) {
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://");
    }

    private static String emptyToNull(String value) {
        if (value == null) return null;
        String s = value.trim();
        return s.isEmpty() ? null : s;
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
        response.setVerified(profile.isVerified());
        return response;
    }
}
