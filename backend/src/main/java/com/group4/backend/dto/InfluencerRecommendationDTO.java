package com.group4.backend.dto;

public class InfluencerRecommendationDTO {
    private Long influencerId;
    private int matchScore;
    private String reason;

    // Getters and Setters
    public Long getInfluencerId() { return influencerId; }
    public void setInfluencerId(Long influencerId) { this.influencerId = influencerId; }
    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
