package com.group4.backend.service.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final int IMAGE_MAX_DIMENSION = 800;

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Transformation transformation = new Transformation()
                .width(IMAGE_MAX_DIMENSION).height(IMAGE_MAX_DIMENSION).crop("limit").quality("auto").fetchFormat("auto");
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"},
                "transformation", transformation);

        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
        String url = (String) result.get("secure_url");
        if (url == null) {
            Object error = result.get("error");
            throw new IOException("Cloudinary upload failed: " + (error != null ? error : "no URL returned"));
        }
        return url;
    }
}
