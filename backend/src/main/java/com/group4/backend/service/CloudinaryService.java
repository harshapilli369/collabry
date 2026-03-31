package com.group4.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads a file to Cloudinary and returns the secure CDN URL.
     *
     * @param file   the multipart image file to upload
     * @param folder the Cloudinary folder for organising uploads
     * @return the secure HTTPS URL of the uploaded image
     * @throws IOException if the upload fails or the file cannot be read
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"},
                "transformation", ObjectUtils.asMap(
                        "width", 800,
                        "height", 800,
                        "crop", "limit",
                        "quality", "auto",
                        "fetch_format", "auto"));

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) result.get("secure_url");
    }
}
