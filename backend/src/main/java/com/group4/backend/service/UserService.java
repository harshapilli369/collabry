package com.group4.backend.service;

import com.group4.backend.dto.SocialLinkRequest;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void linkSocialAccount(Long userId, SocialLinkRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // For this feature, providing a social link verifies the user identity
        user.setVerified(true);
        userRepository.save(user);
    }
}
