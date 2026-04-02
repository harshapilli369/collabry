package com.group4.backend.dto.invitation;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InvitationRequest {

    @NotNull(message = "Influencer ID is required")
    private Long influencerId;

    private String message;

    private BigDecimal proposedAmount;
    private String proposedTimeline;
    private String proposedDeliverables;
    private String platform;
    private Integer expiresInDays;

    public InvitationRequest() {
    }

    public Long getInfluencerId() { return influencerId; }
    public void setInfluencerId(Long influencerId) { this.influencerId = influencerId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public BigDecimal getProposedAmount() { return proposedAmount; }
    public void setProposedAmount(BigDecimal proposedAmount) { this.proposedAmount = proposedAmount; }
    public String getProposedTimeline() { return proposedTimeline; }
    public void setProposedTimeline(String proposedTimeline) { this.proposedTimeline = proposedTimeline; }
    public String getProposedDeliverables() { return proposedDeliverables; }
    public void setProposedDeliverables(String proposedDeliverables) { this.proposedDeliverables = proposedDeliverables; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Integer getExpiresInDays() { return expiresInDays; }
    public void setExpiresInDays(Integer expiresInDays) { this.expiresInDays = expiresInDays; }
}
