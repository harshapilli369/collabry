package com.group4.backend.service;
import com.group4.backend.service.auth.AuthService;

import com.group4.backend.dto.auth.AuthResponse;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.user.UserRepository;
import com.group4.backend.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldAuthenticateAndReturnAuthResponse() {
        LoginRequest request = new LoginRequest("influencer@test.com", "Password1");
        request.setRememberMe(true);
        User user = new User("influencer@test.com", "encodedPass", Role.INFLUENCER);
        user.setId(55L);
        user.setVerified(true);

        when(userRepository.findByEmail("influencer@test.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("influencer@test.com", "INFLUENCER", true)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertAll(
                () -> assertThat(response.getToken()).as("token").isEqualTo("jwt-token"),
                () -> assertThat(response.getEmail()).as("email").isEqualTo("influencer@test.com"),
                () -> assertThat(response.getRole()).as("role").isEqualTo(Role.INFLUENCER),
                () -> assertThat(response.getId()).as("user id").isEqualTo(55L)
        );
    }

    @Test
    void login_shouldThrowWhenUserNotFoundAfterAuthentication() {
        LoginRequest request = new LoginRequest("missing@test.com", "Password1");
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_withRememberMeFalse_shouldPassFalseToTokenGenerator() {
        LoginRequest request = new LoginRequest("user@test.com", "Password1");
        request.setRememberMe(false);
        User user = new User("user@test.com", "encodedPass", Role.INFLUENCER);
        user.setId(1L);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("user@test.com", "INFLUENCER", false)).thenReturn("jwt");

        AuthResponse response = authService.login(request);

        assertThat(response).as("login response").isNotNull();
        verify(jwtUtils).generateToken("user@test.com", "INFLUENCER", false);
    }

    @Test
    void login_withTestBrandEmail_shouldReturnVerifiedTrue() {
        LoginRequest request = new LoginRequest("brand@collabry", "Password1");
        User user = new User("brand@collabry", "encodedPass", Role.BRAND);
        user.setId(1L);
        user.setVerified(false); // not verified in DB but test brand email

        when(userRepository.findByEmail("brand@collabry")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(anyString(), anyString(), eq(false))).thenReturn("jwt");

        AuthResponse response = authService.login(request);

        assertThat(response.isVerified()).isTrue();
    }

    @Test
    void parseRole_withNull_shouldReturnInfluencer() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", (String) null);
        assertThat(result).isEqualTo(Role.INFLUENCER);
    }

    @Test
    void parseRole_withBlankString_shouldReturnInfluencer() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "   ");
        assertThat(result).isEqualTo(Role.INFLUENCER);
    }

    @Test
    void parseRole_withBrand_shouldReturnBrand() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "BRAND");
        assertThat(result).isEqualTo(Role.BRAND);
    }

    @Test
    void parseRole_withLowercaseBrand_shouldReturnBrand() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "brand");
        assertThat(result).isEqualTo(Role.BRAND);
    }

    @Test
    void parseRole_withUser_shouldConvertToInfluencer() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "USER");
        assertThat(result).isEqualTo(Role.INFLUENCER);
    }

    @Test
    void parseRole_withInvalidRole_shouldReturnInfluencer() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "INVALID_ROLE");
        assertThat(result).isEqualTo(Role.INFLUENCER);
    }

    @Test
    void parseRole_withInfluencer_shouldReturnInfluencer() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "INFLUENCER");
        assertThat(result).isEqualTo(Role.INFLUENCER);
    }

    @Test
    void parseRole_withAdmin_shouldReturnAdmin() {
        Role result = ReflectionTestUtils.invokeMethod(authService, "parseRole", "ADMIN");
        assertThat(result).isEqualTo(Role.ADMIN);
    }

    @Test
    void loginWithGoogle_invalidToken_shouldThrowRuntimeException() {
        // fetchEmailFromGoogle will return null for an invalid token (connection will fail)
        // Since we can't mock the private HTTP call easily, we test with a clearly invalid token
        assertThatThrownBy(() -> authService.loginWithGoogle("invalid_token", "BRAND"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Google Token");
    }

}
