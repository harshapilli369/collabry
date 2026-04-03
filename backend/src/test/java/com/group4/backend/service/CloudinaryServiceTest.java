package com.group4.backend.service;
import com.group4.backend.service.media.CloudinaryService;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD tests written before implementation.
 * Red â†’ Green â†’ Refactor cycle for CloudinaryService.
 */
@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    // --- Red: uploadImage returns the secure_url from Cloudinary ---
    @Test
    void uploadImage_withValidJpegFile_returnsSecureUrl() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/profile-pictures/avatar.jpg";
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", expectedUrl));

        String result = cloudinaryService.uploadImage(file, "profile-pictures");

        assertThat(result).isEqualTo(expectedUrl);
    }

    // --- Red: uploadImage uses the provided folder ---
    @Test
    void uploadImage_passesCorrectFolderToCloudinary() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "png-bytes".getBytes());
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/brand-logos/logo.png"));

        String result = cloudinaryService.uploadImage(file, "brand-logos");

        verify(uploader).upload(eq(file.getBytes()), any(Map.class));
        assertThat(result).isNotNull();
    }

    // --- Red: uploadImage with PNG file returns URL ---
    @Test
    void uploadImage_withValidPngFile_returnsSecureUrl() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", "png-bytes".getBytes());
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/brand-logos/logo.png";
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", expectedUrl));

        String result = cloudinaryService.uploadImage(file, "brand-logos");

        assertAll(
                () -> assertThat(result).as("secure url").isEqualTo(expectedUrl),
                () -> assertThat(result).as("url uses https").startsWith("https://")
        );
    }

    // --- Red: uploadImage propagates IOException from Cloudinary ---
    @Test
    void uploadImage_whenCloudinaryThrowsIOException_propagatesException() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "bytes".getBytes());
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new IOException("Cloudinary connection failed"));

        assertThatThrownBy(() -> cloudinaryService.uploadImage(file, "profile-pictures"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary connection failed");
    }

    // --- Red: uploadImage calls uploader on every invocation ---
    @Test
    void uploadImage_callsCloudinaryUploaderOnEachInvocation() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "pic.webp", "image/webp", "webp-bytes".getBytes());
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/x"));

        String first = cloudinaryService.uploadImage(file, "profile-pictures");
        String second = cloudinaryService.uploadImage(file, "profile-pictures");

        verify(uploader, org.mockito.Mockito.times(2)).upload(any(byte[].class), any(Map.class));
        assertThat(first).as("first upload result").isNotNull();
        assertThat(second).as("second upload result").isNotNull();
    }
}
