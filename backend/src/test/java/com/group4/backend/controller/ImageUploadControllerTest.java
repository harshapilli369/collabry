package com.group4.backend.controller;

import com.group4.backend.repository.UserRepository;
import com.group4.backend.security.JwtUtils;
import com.group4.backend.service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TDD tests written before implementation.
 * Red → Green → Refactor cycle for ImageUploadController.
 */
@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CloudinaryService cloudinaryService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    // --- Red: unauthenticated POST request is rejected with 403 ---
    // Spring Security returns 403 (not 401) for unauthenticated POST requests
    // because the missing CSRF token triggers AccessDeniedException before
    // the AuthenticationEntryPoint can respond with 401.
    @Test
    void uploadImage_withoutAuth_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "jpeg-bytes".getBytes());

        mockMvc.perform(multipart("/api/upload/image").file(file))
                .andExpect(status().isForbidden());
    }

    // --- Red: valid image upload returns 200 with URL ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_withValidJpegFile_returns200AndUrl() throws Exception {
        String expectedUrl = "https://res.cloudinary.com/demo/profile-pictures/avatar.jpg";
        when(cloudinaryService.uploadImage(any(), anyString())).thenReturn(expectedUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "jpeg-bytes".getBytes());

        mockMvc.perform(multipart("/api/upload/image").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }

    // --- Red: empty file body returns 400 ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_withEmptyFile_returns400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/upload/image").file(emptyFile).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- Red: non-image content type returns 400 ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_withNonImageContentType_returns400() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());

        mockMvc.perform(multipart("/api/upload/image").file(pdfFile).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- Red: Cloudinary service failure returns 500 ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_whenCloudinaryFails_returns500() throws Exception {
        when(cloudinaryService.uploadImage(any(), anyString()))
                .thenThrow(new IOException("Cloudinary unavailable"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "jpeg-bytes".getBytes());

        mockMvc.perform(multipart("/api/upload/image").file(file).with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- Red: custom folder param is forwarded to service ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_withCustomFolder_returns200() throws Exception {
        String expectedUrl = "https://res.cloudinary.com/demo/brand-logos/logo.png";
        when(cloudinaryService.uploadImage(any(), anyString())).thenReturn(expectedUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", MediaType.IMAGE_PNG_VALUE, "png-bytes".getBytes());

        mockMvc.perform(multipart("/api/upload/image")
                        .file(file)
                        .param("folder", "brand-logos")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }

    // --- Red: PNG file is also accepted ---
    @Test
    @WithMockUser(username = "user@test.com")
    void uploadImage_withValidPngFile_returns200AndUrl() throws Exception {
        String expectedUrl = "https://res.cloudinary.com/demo/profile-pictures/logo.png";
        when(cloudinaryService.uploadImage(any(), anyString())).thenReturn(expectedUrl);

        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", MediaType.IMAGE_PNG_VALUE, "png-bytes".getBytes());

        mockMvc.perform(multipart("/api/upload/image").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }
}
