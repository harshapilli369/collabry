package com.group4.backend.config;

import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void userDetailsServiceReturnsUserWhenEmailExists() {
        User user = new User("a@b.com", "hash", Role.INFLUENCER);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        ApplicationConfig config = new ApplicationConfig(userRepository);
        UserDetailsService uds = config.userDetailsService();
        UserDetails details = uds.loadUserByUsername("a@b.com");

        assertAll(
                () -> assertThat(details.getUsername()).isEqualTo("a@b.com"),
                () -> assertThat(details.getPassword()).isEqualTo("hash"),
                () -> assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_INFLUENCER")
        );
    }

    @Test
    void userDetailsServiceThrowsWhenEmailMissing() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        ApplicationConfig config = new ApplicationConfig(userRepository);
        UserDetailsService uds = config.userDetailsService();

        assertThatThrownBy(() -> uds.loadUserByUsername("missing@b.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void passwordEncoderIsBcrypt() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        PasswordEncoder encoder = config.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void authenticationProviderUsesUserDetailsServiceAndPasswordEncoder() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        assertThat(config.authenticationProvider()).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    void authenticationManagerDelegatesToConfiguration() throws Exception {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        org.springframework.security.authentication.AuthenticationManager am =
                mock(org.springframework.security.authentication.AuthenticationManager.class);
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        when(authConfig.getAuthenticationManager()).thenReturn(am);

        assertThat(config.authenticationManager(authConfig)).isSameAs(am);
    }
}
