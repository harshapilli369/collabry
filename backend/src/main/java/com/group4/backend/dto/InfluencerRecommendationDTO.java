package com.group4.backend.dto;

public class InfluencerRecommendationDTO {
    private Long influencerId;
    private int matchScore;
    private String reason;
    private String name;
    private String niche;
    private String profilePictureUrl;

    // Getters and Setters
    public Long getInfluencerId() { return influencerId; }
    public void setInfluencerId(Long influencerId) { this.influencerId = influencerId; }
    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}
