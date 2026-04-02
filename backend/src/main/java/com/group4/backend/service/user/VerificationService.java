package com.group4.backend.service.user;

import com.group4.backend.dto.VerificationStatusResponse;
import com.group4.backend.dto.admin.AdminVerificationRequestDto;
import com.group4.backend.dto.admin.AdminVerificationProcessRequest;
import com.group4.backend.model.*;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.repository.user.VerificationRequestRepository;
import com.group4.backend.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final EmailService emailService;

    public VerificationService(VerificationRequestRepository verificationRequestRepository,
                               UserRepository userRepository,
                               BrandProfileRepository brandProfileRepository,
                               InfluencerProfileRepository influencerProfileRepository,
                               EmailService emailService) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.userRepository = userRepository;
        this.brandProfileRepository = brandProfileRepository;
        this.influencerProfileRepository = influencerProfileRepository;
        this.emailService = emailService;
    }

    @Transactional
    public VerificationStatusResponse createRequest(Long userId) {
        logger.info("Creating verification request for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isVerified()) {
            logger.warn("User ID {} is already verified", userId);
            throw new IllegalArgumentException("User is already verified");
        }

        Optional<VerificationRequest> existing = verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (existing.isPresent() && existing.get().getStatus() == VerificationRequestStatus.PENDING) {
            logger.warn("User ID {} already has a pending request", userId);
            throw new IllegalArgumentException("You already have a pending verification request");
        }

        VerificationRequest request = new VerificationRequest();
        request.setUserId(userId);
        request.setStatus(VerificationRequestStatus.PENDING);
        request = verificationRequestRepository.save(request);

        logger.info("Verification request created with ID: {} for user ID: {}", request.getId(), userId);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public Optional<VerificationStatusResponse> getLatestRequest(Long userId) {
        return verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AdminVerificationRequestDto> listPendingRequests() {
        logger.info("Listing all pending verification requests");
        List<VerificationRequest> requests = verificationRequestRepository.findByStatus(VerificationRequestStatus.PENDING);
        logger.info("Found {} pending verification requests", requests.size());
        
        return requests.stream()
                .map(request -> {
                    User user = userRepository.findById(request.getUserId()).orElse(null);
                    return new AdminVerificationRequestDto(
                            request.getId(),
                            request.getUserId(),
                            user != null ? user.getEmail() : "Unknown",
                            user != null ? user.getRole() : null,
                            request.getStatus(),
                            request.getAdminReason(),
                            request.getCreatedAt(),
                            request.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void processRequest(Long requestId, AdminVerificationProcessRequest processRequest) {
        VerificationRequest request = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Verification request not found"));

        if (request.getStatus() != VerificationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request has already been processed");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User association lost"));

        if (processRequest.getApproved()) {
            request.setStatus(VerificationRequestStatus.APPROVED);
            user.setVerified(true);
            userRepository.save(user);

            // Sync with profiles if applicable
            if (user.getRole() == Role.BRAND) {
                brandProfileRepository.findByUserId(user.getId()).ifPresent(p -> {
                    p.setVerified(true);
                    brandProfileRepository.save(p);
                });
            } else if (user.getRole() == Role.INFLUENCER) {
                influencerProfileRepository.findByUserId(user.getId()).ifPresent(p -> {
                    p.setVerified(true);
                    influencerProfileRepository.save(p);
                });
            }
        } else {
            request.setStatus(VerificationRequestStatus.REJECTED);
            request.setAdminReason(processRequest.getReason());
        }

        verificationRequestRepository.save(request);

        // Notify user
        emailService.sendVerificationStatusEmail(user.getEmail(), processRequest.getApproved(), processRequest.getReason());
    }

    private VerificationStatusResponse toResponse(VerificationRequest request) {
        return new VerificationStatusResponse(
                request.getStatus(),
                request.getAdminReason(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
