package com.group4.backend.dto.invitation;

import com.group4.backend.model.InvitationStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class InvitationDetailResponse {

    private Long id;
    private Long campaignId;
    private Long influencerId;
    private Long brandId;
    private InvitationStatus status;
    private String brandMessage;
    private BigDecimal proposedAmount;
    private String proposedTimeline;
    private String proposedDeliverables;
    private String platform;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant respondedAt;
    private InvitationCampaignView campaign;

    // Brand profile fields
    private String brandName;
    private String brandLogo;
    private String brandNiche;

    // Influencer profile fields
    private String influencerName;
    private String influencerProfilePicture;
    private String influencerNiche;
    private String influencerRate;

    // Campaign name for convenience
    private String campaignName;

    public InvitationDetailResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getInfluencerId() { return influencerId; }
    public void setInfluencerId(Long influencerId) { this.influencerId = influencerId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    public String getBrandMessage() { return brandMessage; }
    public void setBrandMessage(String brandMessage) { this.brandMessage = brandMessage; }
    public BigDecimal getProposedAmount() { return proposedAmount; }
    public void setProposedAmount(BigDecimal proposedAmount) { this.proposedAmount = proposedAmount; }
    public String getProposedTimeline() { return proposedTimeline; }
    public void setProposedTimeline(String proposedTimeline) { this.proposedTimeline = proposedTimeline; }
    public String getProposedDeliverables() { return proposedDeliverables; }
    public void setProposedDeliverables(String proposedDeliverables) { this.proposedDeliverables = proposedDeliverables; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
    public InvitationCampaignView getCampaign() { return campaign; }
    public void setCampaign(InvitationCampaignView campaign) { this.campaign = campaign; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getBrandLogo() { return brandLogo; }
    public void setBrandLogo(String brandLogo) { this.brandLogo = brandLogo; }
    public String getBrandNiche() { return brandNiche; }
    public void setBrandNiche(String brandNiche) { this.brandNiche = brandNiche; }

    public String getInfluencerName() { return influencerName; }
    public void setInfluencerName(String influencerName) { this.influencerName = influencerName; }
    public String getInfluencerProfilePicture() { return influencerProfilePicture; }
    public void setInfluencerProfilePicture(String influencerProfilePicture) { this.influencerProfilePicture = influencerProfilePicture; }
    public String getInfluencerNiche() { return influencerNiche; }
    public void setInfluencerNiche(String influencerNiche) { this.influencerNiche = influencerNiche; }
    public String getInfluencerRate() { return influencerRate; }
    public void setInfluencerRate(String influencerRate) { this.influencerRate = influencerRate; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
}
