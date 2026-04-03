package com.group4.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenNoAuthorizationHeader_continuesWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).extractUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_whenAuthorizationDoesNotStartWithBearer_continuesWithoutAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).extractUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_whenValidBearerToken_setsAuthentication() throws Exception {
        String jwt = "signed-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.extractUsername(jwt)).thenReturn("user@test.com");

        UserDetails userDetails = User.withUsername("user@test.com")
                .password("n/a")
                .roles("BRAND")
                .build();
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtUtils.isTokenValid(jwt, "user@test.com")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertAll(
                () -> assertThat(SecurityContextHolder.getContext().getAuthentication())
                        .as("authentication type").isInstanceOf(UsernamePasswordAuthenticationToken.class),
                () -> assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).as("authenticated username").isEqualTo("user@test.com")
        );
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenInvalid_doesNotSetAuthentication() throws Exception {
        String jwt = "jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.extractUsername(jwt)).thenReturn("user@test.com");
        UserDetails userDetails = User.withUsername("user@test.com")
                .password("n/a")
                .roles("BRAND")
                .build();
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtUtils.isTokenValid(jwt, "user@test.com")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenSecurityContextAlreadyAuthenticated_skipsLoadingUser() throws Exception {
        UserDetails existing = User.withUsername("existing@test.com")
                .password("n/a")
                .roles("ADMIN")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(existing, null, existing.getAuthorities()));

        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");
        when(jwtUtils.extractUsername("sometoken")).thenReturn("other@test.com");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("existing@test.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenExtractedEmailIsNull_doesNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtils.extractUsername("token")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
