package com.group4.backend.service.user;

import com.group4.backend.dto.admin.AdminVerificationProcessRequest;
import com.group4.backend.model.*;
import com.group4.backend.dto.VerificationStatusResponse;
import com.group4.backend.repository.profile.BrandProfileRepository;
import com.group4.backend.repository.profile.InfluencerProfileRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.repository.user.VerificationRequestRepository;
import com.group4.backend.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BrandProfileRepository brandProfileRepository;

    @Mock
    private InfluencerProfileRepository influencerProfileRepository;

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

        ArgumentCaptor<VerificationRequest> captor = ArgumentCaptor.forClass(VerificationRequest.class);
        verify(verificationRequestRepository).save(captor.capture());
        VerificationRequest saved = captor.getValue();
        assertThat(saved.getUserId()).as("saved user id").isEqualTo(1L);
        assertThat(saved.getStatus()).as("saved status").isEqualTo(VerificationRequestStatus.PENDING);
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

        assertThat(request.getStatus()).as("request status after approval").isEqualTo(VerificationRequestStatus.APPROVED);
        assertThat(user.isVerified()).as("user verified after approval").isTrue();
        assertThat(brand.isVerified()).as("brand profile verified after approval").isTrue();

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

        assertThat(request.getStatus()).as("request status after rejection").isEqualTo(VerificationRequestStatus.REJECTED);
        assertThat(request.getAdminReason()).as("admin rejection reason").isEqualTo("Incomplete profile");
        assertThat(user.isVerified()).as("user not verified after rejection").isFalse();

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

        assertThat(results).as("results size").hasSize(1);
        assertThat(results.get(0).getUserEmail()).as("user email").isEqualTo("influencer@test.com");
        assertThat(results.get(0).getUserRole()).as("user role").isEqualTo(Role.INFLUENCER);
        assertThat(results.get(0).getId()).as("request id").isEqualTo(200L);
    }

    @Test
    void listPendingRequests_whenUserNotFound_shouldReturnUnknownEmail() {
        VerificationRequest request = new VerificationRequest();
        request.setId(300L);
        request.setUserId(99L);
        request.setStatus(VerificationRequestStatus.PENDING);

        when(verificationRequestRepository.findByStatus(VerificationRequestStatus.PENDING))
                .thenReturn(List.of(request));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        var results = verificationService.listPendingRequests();

        assertThat(results).as("results size").hasSize(1);
        assertThat(results.get(0).getUserEmail()).as("fallback email").isEqualTo("Unknown");
        assertThat(results.get(0).getUserRole()).as("fallback role null").isNull();
    }

    @Test
    void listPendingRequests_whenNoPending_shouldReturnEmptyList() {
        when(verificationRequestRepository.findByStatus(VerificationRequestStatus.PENDING))
                .thenReturn(List.of());

        var results = verificationService.listPendingRequests();

        assertThat(results).isEmpty();
    }

    @Test
    void getLatestRequest_whenExists_shouldReturnResponse() {
        VerificationRequest request = new VerificationRequest();
        request.setId(10L);
        request.setUserId(1L);
        request.setStatus(VerificationRequestStatus.PENDING);

        when(verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(request));

        Optional<VerificationStatusResponse> result = verificationService.getLatestRequest(1L);

        assertThat(result).as("latest request present").isPresent();
        assertThat(result.get().getStatus()).as("latest request status").isEqualTo(VerificationRequestStatus.PENDING);
    }

    @Test
    void getLatestRequest_whenNotExists_shouldReturnEmpty() {
        when(verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        Optional<VerificationStatusResponse> result = verificationService.getLatestRequest(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void createRequest_throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.createRequest(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createRequest_throwsWhenPendingRequestExists() {
        User user = new User("user@test.com", "pass", Role.BRAND);
        user.setId(1L);
        user.setVerified(false);

        VerificationRequest existing = new VerificationRequest();
        existing.setStatus(VerificationRequestStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(verificationRequestRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> verificationService.createRequest(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pending verification request");
    }

    @Test
    void processVerification_approvesAndSyncsInfluencer() {
        User user = new User("influencer@test.com", "pass", Role.INFLUENCER);
        user.setId(2L);
        VerificationRequest request = new VerificationRequest();
        request.setId(200L);
        request.setUserId(2L);
        request.setStatus(VerificationRequestStatus.PENDING);

        InfluencerProfile profile = new InfluencerProfile();
        profile.setUserId(2L);
        profile.setVerified(false);

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);
        processRequest.setReason("Good content");

        when(verificationRequestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(influencerProfileRepository.findByUserId(2L)).thenReturn(Optional.of(profile));

        verificationService.processRequest(200L, processRequest);

        assertThat(request.getStatus()).as("request status after influencer approval").isEqualTo(VerificationRequestStatus.APPROVED);
        assertThat(user.isVerified()).as("user verified after approval").isTrue();
        assertThat(profile.isVerified()).as("influencer profile verified after approval").isTrue();

        verify(influencerProfileRepository).save(profile);
        verify(userRepository).save(user);
        verify(emailService).sendVerificationStatusEmail("influencer@test.com", true, "Good content");
    }

    @Test
    void processVerification_throwsWhenRequestNotFound() {
        when(verificationRequestRepository.findById(999L)).thenReturn(Optional.empty());

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);

        assertThatThrownBy(() -> verificationService.processRequest(999L, processRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verification request not found");
    }

    @Test
    void processVerification_throwsWhenAlreadyProcessed() {
        VerificationRequest request = new VerificationRequest();
        request.setId(100L);
        request.setStatus(VerificationRequestStatus.APPROVED);

        when(verificationRequestRepository.findById(100L)).thenReturn(Optional.of(request));

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);

        assertThatThrownBy(() -> verificationService.processRequest(100L, processRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been processed");
    }

    @Test
    void processVerification_throwsWhenUserAssociationLost() {
        VerificationRequest request = new VerificationRequest();
        request.setId(100L);
        request.setUserId(999L);
        request.setStatus(VerificationRequestStatus.PENDING);

        when(verificationRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);

        assertThatThrownBy(() -> verificationService.processRequest(100L, processRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User association lost");
    }

    @Test
    void processVerification_approvedWithNoProfile_shouldNotSyncProfile() {
        User user = new User("brand@test.com", "pass", Role.BRAND);
        user.setId(1L);
        VerificationRequest request = new VerificationRequest();
        request.setId(100L);
        request.setUserId(1L);
        request.setStatus(VerificationRequestStatus.PENDING);

        AdminVerificationProcessRequest processRequest = new AdminVerificationProcessRequest();
        processRequest.setApproved(true);
        processRequest.setReason("OK");

        when(verificationRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(brandProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        verificationService.processRequest(100L, processRequest);

        assertThat(user.isVerified()).isTrue();
        verify(brandProfileRepository, never()).save(any());
    }
}
