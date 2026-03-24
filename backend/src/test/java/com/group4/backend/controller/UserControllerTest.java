package com.group4.backend.controller;

import com.group4.backend.dto.InfluencerSearchResult;
import com.group4.backend.model.Role;
import com.group4.backend.model.User;
import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    private User brandUser;
    private User influencerUser;

    @BeforeEach
    void setUp() {
        brandUser = new User("brand@test.com", "p", Role.BRAND);
        brandUser.setId(10L);
        influencerUser = new User("inf@test.com", "p", Role.INFLUENCER);
        influencerUser.setId(20L);
    }

    @Test
    @WithMockUser(username = "brand@test.com")
    void listInfluencers_asBrand_returns200() throws Exception {
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.of(brandUser));
        InfluencerSearchResult row = new InfluencerSearchResult(1L, "i@test.com", "Influencer One");
        when(userService.listInfluencers()).thenReturn(List.of(row));

        mockMvc.perform(get("/api/users/influencers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("i@test.com"));
    }

    @Test
    @WithMockUser(username = "inf@test.com")
    void listInfluencers_asInfluencer_returns403() throws Exception {
        when(userRepository.findByEmail("inf@test.com")).thenReturn(Optional.of(influencerUser));

        mockMvc.perform(get("/api/users/influencers"))
                .andExpect(status().isForbidden());
    }
}
