package com.group4.backend.service.user;

import com.group4.backend.dto.profile.InfluencerSearchResult;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.model.InfluencerProfile;
import com.group4.backend.model.BrandProfile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final BrandProfileRepository brandProfileRepository;

    public UserService(UserRepository userRepository, 
                       InfluencerProfileRepository influencerProfileRepository,
                       BrandProfileRepository brandProfileRepository) {
        this.userRepository = userRepository;
        this.influencerProfileRepository = influencerProfileRepository;
        this.brandProfileRepository = brandProfileRepository;
    }

    /** List all influencers with id, email, displayName (for brands to find user IDs). */
    public List<InfluencerSearchResult> listInfluencers() {
        return userRepository.findByRole(Role.INFLUENCER).stream()
                .map(user -> {
                    String displayName = influencerProfileRepository.findByUserId(user.getId())
                            .map(p -> p.getName())
                            .orElse(user.getEmail());
                    return new InfluencerSearchResult(user.getId(), user.getEmail(), displayName);
                })
                .collect(Collectors.toList());
    }

    public void linkSocialAccount(Long userId, SocialLinkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getRole() == Role.INFLUENCER) {
            influencerProfileRepository.findByUserId(userId)
                    .ifPresent(profile -> applyInfluencerSocialLink(profile, request));
        } else if (user.getRole() == Role.BRAND) {
            brandProfileRepository.findByUserId(userId)
                    .ifPresent(profile -> applyBrandSocialLink(profile, request));
        }
    }

    private void applyInfluencerSocialLink(InfluencerProfile profile, SocialLinkRequest request) {
        String platform = request.getPlatform().toUpperCase();
        switch (platform) {
            case "INSTAGRAM" -> profile.setInstagramHandle(request.getHandle());
            case "YOUTUBE" -> profile.setYoutubeHandle(request.getHandle());
            case "TIKTOK" -> profile.setTiktokHandle(request.getHandle());
            default -> { /* unknown platform: no-op */ }
        }
        influencerProfileRepository.save(profile);
    }

    private void applyBrandSocialLink(BrandProfile profile, SocialLinkRequest request) {
        String platform = request.getPlatform().toUpperCase();
        switch (platform) {
            case "INSTAGRAM" -> profile.setInstagramUrl(request.getHandle());
            case "LINKEDIN" -> profile.setLinkedInUrl(request.getHandle());
            case "TWITTER" -> profile.setTwitterUrl(request.getHandle());
            default -> { /* unknown platform: no-op */ }
        }
        brandProfileRepository.save(profile);
    }
}
