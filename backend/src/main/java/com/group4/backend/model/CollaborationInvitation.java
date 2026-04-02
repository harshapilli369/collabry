package com.group4.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invitations")
public class CollaborationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "influencer_id", nullable = false)
    private Long influencerId;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "brand_message", columnDefinition = "TEXT")
    private String brandMessage;

    @Column(name = "proposed_amount", precision = 12, scale = 2)
    private BigDecimal proposedAmount;

    @Column(name = "proposed_timeline", length = 500)
    private String proposedTimeline;

    @Column(name = "proposed_deliverables", columnDefinition = "TEXT")
    private String proposedDeliverables;

    @Column(length = 100)
    private String platform;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "deliverable_status", length = 20)
    private DeliverableStatus deliverableStatus = DeliverableStatus.NOT_STARTED;

    @Column(name = "content_link", length = 1000)
    private String contentLink;

    @Column(name = "deliverable_notes", columnDefinition = "TEXT")
    private String deliverableNotes;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public CollaborationInvitation() {
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
    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public DeliverableStatus getDeliverableStatus() { return deliverableStatus; }
    public void setDeliverableStatus(DeliverableStatus deliverableStatus) { this.deliverableStatus = deliverableStatus; }
    public String getContentLink() { return contentLink; }
    public void setContentLink(String contentLink) { this.contentLink = contentLink; }
    public String getDeliverableNotes() { return deliverableNotes; }
    public void setDeliverableNotes(String deliverableNotes) { this.deliverableNotes = deliverableNotes; }
}
