package com.group4.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.admin.AdminDashboardResponse;
import com.group4.backend.dto.admin.AdminRecentSignupDto;
import com.group4.backend.dto.admin.AdminUserPageResponse;
import com.group4.backend.dto.admin.AdminUserSummaryDto;
import com.group4.backend.model.Role;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.admin.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AdminService adminService;
    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_asAdmin_returns200() throws Exception {
        var dash = new AdminDashboardResponse();
        dash.setBrandCount(1);
        dash.setInfluencerCount(2);
        dash.setCampaignCount(3);
        var signup = new AdminRecentSignupDto();
        signup.setEmail("x@test.com");
        dash.setRecentSignups(List.of(signup));
        dash.setActiveCollaborations(List.of());
        dash.setPaymentsByStatus(Map.of("PENDING", 1L));
        when(adminService.buildDashboard()).thenReturn(dash);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandCount").value(1))
                .andExpect(jsonPath("$.influencerCount").value(2))
                .andExpect(jsonPath("$.campaignCount").value(3))
                .andExpect(jsonPath("$.recentSignups[0].email").value("x@test.com"));
    }

    @Test
    @WithMockUser(roles = "BRAND")
    void dashboard_asBrand_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void users_asAdmin_returns200() throws Exception {
        var page = new AdminUserPageResponse();
        page.setContent(List.of());
        page.setTotalElements(0);
        page.setTotalPages(0);
        page.setNumber(0);
        page.setSize(20);
        when(adminService.listUsers(any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_asAdmin_returns200() throws Exception {
        var summary = new AdminUserSummaryDto();
        summary.setId(5L);
        summary.setEmail("u@test.com");
        summary.setRole(Role.INFLUENCER);
        summary.setActive(false);
        summary.setFlagged(true);
        when(adminService.updateUserStatus(5L, false, true)).thenReturn(summary);

        mockMvc.perform(put("/api/admin/users/5").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false,\"flagged\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("u@test.com"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_illegalArgument_returns400() throws Exception {
        when(adminService.updateUserStatus(1L, false, false))
                .thenThrow(new IllegalArgumentException("Cannot modify admin accounts"));

        mockMvc.perform(put("/api/admin/users/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false, "flagged", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot modify admin accounts"));
    }
}
