package com.group4.backend.service;

import com.group4.backend.dto.InfluencerSearchResult;
import com.group4.backend.dto.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.InfluencerProfileRepository;
import com.group4.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InfluencerProfileRepository influencerProfileRepository;

    public UserService(UserRepository userRepository, InfluencerProfileRepository influencerProfileRepository) {
        this.userRepository = userRepository;
        this.influencerProfileRepository = influencerProfileRepository;
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

        // For this feature, providing a social link verifies the user identity
        user.setVerified(true);
        userRepository.save(user);
    }
}
