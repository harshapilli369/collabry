package com.group4.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class InfluencerProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private Integer age;
    private String location;
    private String niche;
    private String bio;
    private String profilePictureUrl;
    private String instagramHandle;
    private String youtubeHandle;
    private String tiktokHandle;
    private BigDecimal rate;
    private Long followerCount;
    private BigDecimal engagementRate;
    private String audienceInfo;
    private boolean isComplete;
    private boolean openToCollaborations = true;
    private Instant createdAt;
    private Instant updatedAt;
    private Double averageRating;
    private Integer totalRatings;
    private List<RatingResponse> recentReviews;

    public InfluencerProfileResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { isComplete = complete; }

    public boolean isOpenToCollaborations() { return openToCollaborations; }
    public void setOpenToCollaborations(boolean openToCollaborations) { this.openToCollaborations = openToCollaborations; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    public List<RatingResponse> getRecentReviews() { return recentReviews; }
    public void setRecentReviews(List<RatingResponse> recentReviews) { this.recentReviews = recentReviews; }
}
