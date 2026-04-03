package com.group4.backend.service;
import com.group4.backend.service.auth.RegistrationService;
import com.group4.backend.service.email.EmailService;

import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.dto.auth.SignupResponse;
import com.group4.backend.exception.DuplicateEmailException;
import com.group4.backend.model.PendingSignup;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.user.PendingSignupRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PendingSignupRepository pendingSignupRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registrationService, "confirmationBaseUrl", "http://localhost:5173");
    }

    @Test
    void register_shouldCreatePendingSignupAndSendEmail() {
        SignupRequest request = new SignupRequest("brand@test.com", "Password1", Role.BRAND);
        when(userRepository.existsByEmail("brand@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encodedPass");

        SignupResponse response = registrationService.register(request);

        ArgumentCaptor<PendingSignup> pendingCaptor = ArgumentCaptor.forClass(PendingSignup.class);
        verify(pendingSignupRepository).save(pendingCaptor.capture());
        PendingSignup saved = pendingCaptor.getValue();
        assertAll(
                () -> assertThat(saved.getEmail()).as("email").isEqualTo("brand@test.com"),
                () -> assertThat(saved.getEncodedPassword()).as("encoded password").isEqualTo("encodedPass"),
                () -> assertThat(saved.getRole()).as("role").isEqualTo(Role.BRAND),
                () -> assertThat(saved.getToken()).as("token not blank").isNotBlank(),
                () -> assertThat(saved.getExpiresAt()).as("expiry in future").isAfter(Instant.now()),
                () -> assertThat(response.getMessage()).as("response message").contains("Check your email to confirm your account")
        );
        verify(emailService).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void register_shouldThrowWhenRoleIsNotAllowed() {
        SignupRequest request = new SignupRequest("user@test.com", "Password1", Role.ADMIN);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role must be BRAND or INFLUENCER");
    }

    @Test
    void register_shouldThrowOnDuplicateEmail() {
        SignupRequest request = new SignupRequest("brand@test.com", "Password1", Role.BRAND);
        when(userRepository.existsByEmail("brand@test.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("already exists");
        verify(pendingSignupRepository, never()).save(any(PendingSignup.class));
    }

    @Test
    void register_shouldThrowWhenEmailDeliveryFails() {
        SignupRequest request = new SignupRequest("brand@test.com", "Password1", Role.BRAND);
        when(userRepository.existsByEmail("brand@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encodedPass");
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendConfirmationEmail(anyString(), anyString());

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Could not send confirmation email");
    }

    @Test
    void confirmEmail_shouldCreateUserDeletePendingAndReturnAuthResponse() {
        PendingSignup pending = new PendingSignup(
                "brand@test.com", "encodedPass", Role.BRAND,
                "token-123", Instant.now().plusSeconds(3600));
        User created = new User("brand@test.com", "encodedPass", Role.BRAND);
        created.setId(77L);
        created.setVerified(true);

        when(pendingSignupRepository.findByToken("token-123")).thenReturn(Optional.of(pending));
        when(userRepository.existsByEmail("brand@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(created);
        when(jwtUtils.generateToken("brand@test.com", "BRAND", false)).thenReturn("jwt-token");

        AuthResponse response = registrationService.confirmEmail("token-123");

        assertAll(
                () -> assertThat(response.getToken()).as("jwt token").isEqualTo("jwt-token"),
                () -> assertThat(response.getEmail()).as("email").isEqualTo("brand@test.com"),
                () -> assertThat(response.getId()).as("user id").isEqualTo(77L),
                () -> assertThat(response.getRole()).as("role").isEqualTo(Role.BRAND),
                () -> assertThat(response.isVerified()).as("verified flag").isTrue()
        );
        verify(pendingSignupRepository).delete(pending);
    }

    @Test
    void confirmEmail_shouldThrowWhenTokenMissing() {
        when(pendingSignupRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.confirmEmail("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired confirmation link");
    }

    @Test
    void confirmEmail_shouldDeletePendingAndThrowWhenExpired() {
        PendingSignup expired = new PendingSignup(
                "brand@test.com", "encodedPass", Role.BRAND,
                "expired-token", Instant.now().minusSeconds(5));
        when(pendingSignupRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> registrationService.confirmEmail("expired-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");
        verify(pendingSignupRepository).delete(expired);
    }

    @Test
    void confirmEmail_shouldDeletePendingAndThrowWhenEmailAlreadyExists() {
        PendingSignup pending = new PendingSignup(
                "brand@test.com", "encodedPass", Role.BRAND,
                "token-123", Instant.now().plusSeconds(3600));
        when(pendingSignupRepository.findByToken("token-123")).thenReturn(Optional.of(pending));
        when(userRepository.existsByEmail("brand@test.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.confirmEmail("token-123"))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("already exists");
        verify(pendingSignupRepository).delete(pending);
    }
}
