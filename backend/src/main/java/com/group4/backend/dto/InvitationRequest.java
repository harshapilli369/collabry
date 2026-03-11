package com.group4.backend.dto;

import jakarta.validation.constraints.NotNull;

public class InvitationRequest {

    @NotNull(message = "Influencer ID is required")
    private Long influencerId;

    private String message;

    public InvitationRequest() {
    }

    public Long getInfluencerId() { return influencerId; }
    public void setInfluencerId(Long influencerId) { this.influencerId = influencerId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
