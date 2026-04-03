package com.group4.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.backend.dto.auth.LoginRequest;
import com.group4.backend.dto.profile.BrandProfileRequest;
import com.group4.backend.model.BudgetRange;
import com.group4.backend.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BrandProfileIT {

    private static final String SEED_BRAND_EMAIL = "brand@collabry.com";
    private static final String SEED_INFLUENCER_EMAIL = "influencer@collabry.com";
    private static final String SEED_DEFAULT_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void brand_updatesAndReadsMe_publicProfileByUserId() throws Exception {
        String brandToken = login(SEED_BRAND_EMAIL);
        long brandUserId = userRepository.findByEmail(SEED_BRAND_EMAIL).orElseThrow().getId();

        String uniqueName = "IT Brand Co " + UUID.randomUUID();
        BrandProfileRequest req = new BrandProfileRequest();
        req.setName(uniqueName);
        req.setIndustry("Technology");
        req.setWebsite("https://it-brand.example.com");
        req.setEmail("brand@collabry.com");
        req.setDescription("Integration test brand profile");
        req.setBudgetRange(BudgetRange.ONE_K_5K);

        mockMvc.perform(put("/api/brands/me")
                        .header("Authorization", "Bearer " + brandToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueName))
                .andExpect(jsonPath("$.industry").value("Technology"));

        mockMvc.perform(get("/api/brands/me").header("Authorization", "Bearer " + brandToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueName));

        String influencerToken = login(SEED_INFLUENCER_EMAIL);
        mockMvc.perform(get("/api/brands/{id}/profile", brandUserId)
                        .header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueName));
    }

    @Test
    void influencer_forbiddenOnBrandMeEndpoints() throws Exception {
        String influencerToken = login(SEED_INFLUENCER_EMAIL);
        mockMvc.perform(get("/api/brands/me").header("Authorization", "Bearer " + influencerToken))
                .andExpect(status().isForbidden());

        BrandProfileRequest req = new BrandProfileRequest();
        req.setName("X");
        req.setIndustry("Y");
        req.setWebsite("https://x.com");
        req.setEmail("x@x.com");
        mockMvc.perform(put("/api/brands/me")
                        .header("Authorization", "Bearer " + influencerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    private String login(String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, SEED_DEFAULT_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }
}
