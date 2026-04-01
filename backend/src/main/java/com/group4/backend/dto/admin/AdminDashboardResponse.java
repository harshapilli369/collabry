package com.group4.backend.dto.admin;

import java.util.List;
import java.util.Map;

public class AdminDashboardResponse {

    private long brandCount;
    private long influencerCount;
    private long campaignCount;
    private List<AdminRecentSignupDto> recentSignups;
    private List<AdminActiveCollaborationDto> activeCollaborations;
    private Map<String, Long> paymentsByStatus;

    public AdminDashboardResponse() {
    }

    public long getBrandCount() {
        return brandCount;
    }

    public void setBrandCount(long brandCount) {
        this.brandCount = brandCount;
    }

    public long getInfluencerCount() {
        return influencerCount;
    }

    public void setInfluencerCount(long influencerCount) {
        this.influencerCount = influencerCount;
    }

    public long getCampaignCount() {
        return campaignCount;
    }

    public void setCampaignCount(long campaignCount) {
        this.campaignCount = campaignCount;
    }

    public List<AdminRecentSignupDto> getRecentSignups() {
        return recentSignups;
    }

    public void setRecentSignups(List<AdminRecentSignupDto> recentSignups) {
        this.recentSignups = recentSignups;
    }

    public List<AdminActiveCollaborationDto> getActiveCollaborations() {
        return activeCollaborations;
    }

    public void setActiveCollaborations(List<AdminActiveCollaborationDto> activeCollaborations) {
        this.activeCollaborations = activeCollaborations;
    }

    public Map<String, Long> getPaymentsByStatus() {
        return paymentsByStatus;
    }

    public void setPaymentsByStatus(Map<String, Long> paymentsByStatus) {
        this.paymentsByStatus = paymentsByStatus;
    }
}
