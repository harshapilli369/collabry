package com.group4.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.admin.AdminUserUpdateRequest;
import com.group4.backend.dto.auth.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiIT {

    private static final String SEED_ADMIN_EMAIL = "admin@collabry.com";
    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void admin_canLoadDashboardAndUserPage() throws Exception {
        String adminToken = loginAccessToken(SEED_ADMIN_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandCount").exists())
                .andExpect(jsonPath("$.influencerCount").exists())
                .andExpect(jsonPath("$.campaignCount").exists())
                .andExpect(jsonPath("$.paymentsByStatus").exists());

        mockMvc.perform(get("/api/admin/users").param("page", "0").param("size", "10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void brand_forbiddenOnAdminEndpoints() throws Exception {
        String brandToken = loginAccessToken(SEED_BRAND_EMAIL, SEED_DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canUpdateUserActiveAndFlagged() throws Exception {
        String adminToken = loginAccessToken(SEED_ADMIN_EMAIL, SEED_DEFAULT_PASSWORD);

        MvcResult page = mockMvc.perform(get("/api/admin/users").param("page", "0").param("size", "50")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(page.getResponse().getContentAsString());
        JsonNode content = root.get("content");
        assertThat(content.isArray()).isTrue();

        long targetId = -1;
        for (JsonNode row : content) {
            if ("influencer@collabry.com".equals(row.get("email").asText())) {
                targetId = row.get("id").asLong();
                break;
            }
        }
        assertThat(targetId).isPositive();

        AdminUserUpdateRequest body = new AdminUserUpdateRequest();
        body.setActive(true);
        body.setFlagged(false);

        mockMvc.perform(put("/api/admin/users/{id}", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetId))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.flagged").value(false));
    }

    private String loginAccessToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
