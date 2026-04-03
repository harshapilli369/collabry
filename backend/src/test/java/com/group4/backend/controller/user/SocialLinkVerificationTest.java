package com.group4.backend.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.controller.support.CurrentUserProvider;
import com.group4.backend.dto.profile.SocialLinkRequest;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.user.VerificationService;
import com.group4.backend.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class SocialLinkVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private VerificationService verificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("influencer@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.INFLUENCER);
        testUser.setVerified(false);

        Mockito.when(currentUserProvider.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "influencer@example.com", roles = "INFLUENCER")
    void shouldLinkSocialAccount() throws Exception {
        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        request.setHandle("@test_influencer");

        mockMvc.perform(put("/api/users/me/link-social")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).linkSocialAccount(eq(1L), any(SocialLinkRequest.class));
    }

    @Test
    @WithMockUser(username = "influencer@example.com", roles = "INFLUENCER")
    void shouldFailIfHandleMissing() throws Exception {
        SocialLinkRequest request = new SocialLinkRequest();
        request.setPlatform("INSTAGRAM");
        // No handle provided

        mockMvc.perform(put("/api/users/me/link-social")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
