package com.group4.backend.dto.profile;

import jakarta.validation.constraints.NotBlank;

public class SocialLinkRequest {
    @NotBlank(message = "Platform is required")
    private String platform;

    @NotBlank(message = "Handle is required")
    private String handle;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
