package com.group4.backend.dto.profile;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class InfluencerProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 13, message = "Age must be at least 13")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;

    @NotBlank(message = "Location is required")
    @Size(max = 200)
    private String location;

    @NotBlank(message = "Niche is required")
    @Size(max = 100)
    private String niche;

    @Size(max = 2000)
    private String bio;

    @Size(max = 500)
    private String profilePictureUrl;

    @Size(max = 100)
    private String instagramHandle;

    @Size(max = 100)
    private String youtubeHandle;

    @Size(max = 100)
    private String tiktokHandle;

    private BigDecimal rate;

    private Long followerCount;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal engagementRate;

    @Size(max = 2000)
    private String audienceInfo;

    private boolean saveAsDraft = false;

    public InfluencerProfileRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getInstagramHandle() { return instagramHandle; }
    public void setInstagramHandle(String instagramHandle) { this.instagramHandle = instagramHandle; }

    public String getYoutubeHandle() { return youtubeHandle; }
    public void setYoutubeHandle(String youtubeHandle) { this.youtubeHandle = youtubeHandle; }

    public String getTiktokHandle() { return tiktokHandle; }
    public void setTiktokHandle(String tiktokHandle) { this.tiktokHandle = tiktokHandle; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public Long getFollowerCount() { return followerCount; }
    public void setFollowerCount(Long followerCount) { this.followerCount = followerCount; }

    public BigDecimal getEngagementRate() { return engagementRate; }
    public void setEngagementRate(BigDecimal engagementRate) { this.engagementRate = engagementRate; }

    public String getAudienceInfo() { return audienceInfo; }
    public void setAudienceInfo(String audienceInfo) { this.audienceInfo = audienceInfo; }

    public boolean isSaveAsDraft() { return saveAsDraft; }
    public void setSaveAsDraft(boolean saveAsDraft) { this.saveAsDraft = saveAsDraft; }
}
