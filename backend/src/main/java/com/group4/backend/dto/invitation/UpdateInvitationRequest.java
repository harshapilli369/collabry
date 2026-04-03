package com.group4.backend.dto.invitation;

import java.math.BigDecimal;

public class UpdateInvitationRequest {

    private String message;
    private BigDecimal proposedAmount;
    private String proposedTimeline;
    private String proposedDeliverables;
    private String platform;

    public UpdateInvitationRequest() {
    }

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
}
