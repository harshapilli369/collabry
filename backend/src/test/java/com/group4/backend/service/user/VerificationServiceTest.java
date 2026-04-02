package com.group4.backend.service.user;

import com.group4.backend.dto.admin.AdminVerificationProcessRequest;
import com.group4.backend.model.*;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.repository.user.VerificationRequestRepository;
import com.group4.backend.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BrandProfileRepository brandProfileRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void requestVerification_createsPendingRequest() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(1L);
        user.setVerified(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(i -> i.getArgument(0));

        verificationService.createRequest(1L);

        verify(verificationRequestRepository).save(argThat(req -> 
            req.getUserId().equals(1L) && req.getStatus() == VerificationRequestStatus.PENDING
        ));
    }

    @Test
    void requestVerification_throwsIfAlreadyVerified() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(1L);
        user.setVerified(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> verificationService.createRequest(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is already verified");
    }

    @Test
    void processVerification_approvesAndSyncsBrand() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(1L);
        VerificationRequest request = new VerificationRequest();
        request.setUserId(1L);
        request.setId(100L);
        request.setUserId(1L);
        request.setStatus(VerificationRequestStatus.PENDING);

        BrandProfile brand = new BrandProfile();
        brand.setUserId(1L);
        brand.setVerified(false);

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);
        processRequest.setReason("Looks good");

        when(verificationRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(1L)).thenReturn(Optional.of(brand));

        verificationService.processRequest(100L, processRequest);

        assertThat(request.getStatus()).isEqualTo(VerificationRequestStatus.APPROVED);
        assertThat(user.isVerified()).isTrue();
        assertThat(brand.isVerified()).isTrue();
        
        verify(verificationRequestRepository).save(request);
        verify(userRepository).save(user);
        verify(brandProfileRepository).save(brand);
        verify(emailService).sendVerificationStatusEmail(eq("brand@test.com"), eq(true), anyString());
    }

    @Test
    void processVerification_rejectsWithReason() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(1L);
        VerificationRequest request = new VerificationRequest();
        request.setUserId(1L);
        request.setId(100L);
        request.setUserId(1L);
        request.setStatus(VerificationRequestStatus.PENDING);

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(false);
        processRequest.setReason("Incomplete profile");

        when(verificationRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        verificationService.processRequest(100L, processRequest);

        assertThat(request.getStatus()).isEqualTo(VerificationRequestStatus.REJECTED);
        assertThat(request.getAdminReason()).isEqualTo("Incomplete profile");
        assertThat(user.isVerified()).isFalse();

        verify(verificationRequestRepository).save(request);
        verify(emailService).sendVerificationStatusEmail("brand@test.com", false, "Incomplete profile");
    }

    @Test
    void listPendingRequests_returnsDtoWithUserDetails() {
        User user = new User("influencer@test.com", "pass", Role.INFLUENCER);
        user.setId(2L);
        VerificationRequest request = new VerificationRequest();
        request.setId(200L);
        request.setUserId(2L);
        request.setStatus(VerificationRequestStatus.PENDING);

        when(verificationRequestRepository.findByStatus(VerificationRequestStatus.PENDING))
                .thenReturn(java.util.List.of(request));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        java.util.List<com.group4.backend.dto.admin.AdminVerificationRequestDto> results = verificationService.listPendingRequests();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserEmail()).isEqualTo("influencer@test.com");
        assertThat(results.get(0).getUserRole()).isEqualTo(Role.INFLUENCER);
        assertThat(results.get(0).getId()).isEqualTo(200L);
    }
}
