package com.group4.backend.service;
import com.group4.backend.service.auth.PasswordResetService;
import com.group4.backend.service.email.EmailService;

import com.group4.backend.model.PasswordResetToken;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.user.PasswordResetTokenRepository;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "confirmationBaseUrl", "http://localhost:5173");
    }

    @Test
    void forgotPassword_shouldSaveTokenAndSendEmail() {
        User user = new User("user@test.com", "encodedPass", Role.USER);
        user.setId(10L);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        passwordResetService.forgotPassword("user@test.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUser()).isEqualTo(user);
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.forgotPassword("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_shouldThrowWhenTokenMissing() {
        when(tokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("bad-token", "NewPassword1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Token");
    }

    @Test
    void resetPassword_shouldDeleteTokenAndThrowWhenExpired() {
        User user = new User("user@test.com", "old", Role.USER);
        PasswordResetToken token = new PasswordResetToken("token-1", user);
        token.setExpiryDate(new Date(System.currentTimeMillis() - 1_000));
        when(tokenRepository.findByToken("token-1")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("token-1", "NewPassword1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token Expired");
        verify(tokenRepository).delete(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_shouldEncodeSaveUserAndDeleteToken() {
        User user = new User("user@test.com", "old", Role.USER);
        PasswordResetToken token = new PasswordResetToken("token-1", user);
        token.setExpiryDate(new Date(System.currentTimeMillis() + 60_000));
        when(tokenRepository.findByToken("token-1")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("encodedNewPassword");

        passwordResetService.resetPassword("token-1", "NewPassword1");

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }
}
