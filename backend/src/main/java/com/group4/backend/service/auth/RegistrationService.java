package com.group4.backend.service.auth;

import com.group4.backend.service.email.EmailService;
import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.SignupRequest;
import com.group4.backend.dto.auth.SignupResponse;
import com.group4.backend.exception.DuplicateEmailException;
import com.group4.backend.model.PendingSignup;
import com.group4.backend.model.User;
import com.group4.backend.repository.user.PendingSignupRepository;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegistrationService {

    private static final int CONFIRMATION_EXPIRY_HOURS = 24;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final String TEST_BRAND_EMAIL = "brand@collabry";

    private final UserRepository userRepository;
    private final PendingSignupRepository pendingSignupRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.confirmation.base-url:http://localhost:5173}")
    private String confirmationBaseUrl;

    public RegistrationService(UserRepository userRepository,
                                PendingSignupRepository pendingSignupRepository,
                                JwtUtils jwtUtils,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.pendingSignupRepository = pendingSignupRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Register: save as pending only; send confirmation email.
     * User is created only after they confirm.
     */
    public SignupResponse register(SignupRequest request) {
        if (!SignupRequest.isAllowedRole(request.getRole())) {
            throw new IllegalArgumentException("Role must be BRAND or INFLUENCER");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }
        pendingSignupRepository.deleteByEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String confirmationToken = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(CONFIRMATION_EXPIRY_HOURS * SECONDS_PER_HOUR);
        PendingSignup pending = new PendingSignup(
                request.getEmail(), encodedPassword, request.getRole(), confirmationToken, expiresAt);
        pendingSignupRepository.save(pending);

        String confirmationLink = confirmationBaseUrl + "/confirm-email?token=" + confirmationToken;
        try {
            emailService.sendConfirmationEmail(request.getEmail(), confirmationLink);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not send confirmation email. Please check SMTP settings or try again later. "
                            + e.getMessage(), e);
        }

        return new SignupResponse("Check your email to confirm your account. The link expires in "
                + CONFIRMATION_EXPIRY_HOURS + " hours.");
    }

    /**
     * Confirm email: create User from pending signup, then delete pending.
     * Returns JWT so frontend can log in immediately.
     */
    public AuthResponse confirmEmail(String token) {
        PendingSignup pending = pendingSignupRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired confirmation link."));
        if (pending.isExpired()) {
            pendingSignupRepository.delete(pending);
            throw new RuntimeException("Confirmation link has expired. Please sign up again.");
        }
        if (userRepository.existsByEmail(pending.getEmail())) {
            pendingSignupRepository.delete(pending);
            throw new DuplicateEmailException("An account with this email already exists.");
        }
        User user = new User(pending.getEmail(), pending.getEncodedPassword(), pending.getRole());
        user = userRepository.save(user);
        pendingSignupRepository.delete(pending);

        String jwtToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), false);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole(), user.getId(),
                isEffectivelyVerified(user));
    }

    private static boolean isEffectivelyVerified(User user) {
        return user.isVerified() || TEST_BRAND_EMAIL.equalsIgnoreCase(user.getEmail());
    }
}
