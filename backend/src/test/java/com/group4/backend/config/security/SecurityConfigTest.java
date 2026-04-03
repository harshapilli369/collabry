package com.group4.backend.config.security;

import com.group4.backend.security.JwtAuthenticationFilter;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.testsupport.SliceTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(classes = SliceTestApplication.class)
@Import({SecurityConfig.class, SecurityConfigTest.JwtAndAuthBeans.class})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Configuration
    static class JwtAndAuthBeans {
        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            // Real filter so FilterChain continues when there is no Bearer token (a plain mock would not call doFilter).
            return new JwtAuthenticationFilter(mock(JwtUtils.class), mock(UserDetailsService.class));
        }

        @Bean
        AuthenticationProvider authenticationProvider() {
            return mock(AuthenticationProvider.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void corsConfigurationSourceRegistersExpectedOriginsAndHeaders() {
        SecurityConfig securityConfig = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(AuthenticationProvider.class));

        UrlBasedCorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        CorsConfiguration cors = source.getCorsConfiguration(request);
        assertAll(
                () -> assertThat(cors).as("CORS config exists").isNotNull(),
                () -> assertThat(cors.getAllowedOrigins()).as("allowed origins").contains("http://localhost:5173", "http://localhost:8073"),
                () -> assertThat(cors.getAllowedMethods()).as("allowed methods").contains("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                () -> assertThat(cors.getAllowedHeaders()).as("allowed headers").contains("Authorization", "Content-Type"),
                () -> assertThat(cors.getAllowCredentials()).as("allow credentials").isTrue()
        );
    }

    @Test
    void securityFilterChainPermitsAuthEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isNotEqualTo(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void securityFilterChainRequiresAuthenticationForOtherApiPaths() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
    }
}
