package com.group4.backend.dto;

import java.math.BigDecimal;

public class NegotiationRequest {

    private BigDecimal proposedAmount;
    private String proposedTimeline;
    private String proposedDeliverables;

    public NegotiationRequest() {
    }

    public BigDecimal getProposedAmount() { return proposedAmount; }
    public void setProposedAmount(BigDecimal proposedAmount) { this.proposedAmount = proposedAmount; }
    public String getProposedTimeline() { return proposedTimeline; }
    public void setProposedTimeline(String proposedTimeline) { this.proposedTimeline = proposedTimeline; }
    public String getProposedDeliverables() { return proposedDeliverables; }
    public void setProposedDeliverables(String proposedDeliverables) { this.proposedDeliverables = proposedDeliverables; }
}
