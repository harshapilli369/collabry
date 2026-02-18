package com.group4.backend.dto;

import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.CampaignGoal;
import com.group4.backend.model.CampaignStatus;

import java.time.Instant;
import java.time.LocalDate;

public class CampaignResponse {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private BudgetRange budgetRange;
    private CampaignStatus status;
    private CampaignGoal campaignGoal;
    private String preferredContentTypes;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfInfluencers;
    private Instant createdAt;
    private Instant updatedAt;

    public CampaignResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BudgetRange getBudgetRange() { return budgetRange; }
    public void setBudgetRange(BudgetRange budgetRange) { this.budgetRange = budgetRange; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public CampaignGoal getCampaignGoal() { return campaignGoal; }
    public void setCampaignGoal(CampaignGoal campaignGoal) { this.campaignGoal = campaignGoal; }

    public String getPreferredContentTypes() { return preferredContentTypes; }
    public void setPreferredContentTypes(String preferredContentTypes) { this.preferredContentTypes = preferredContentTypes; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getNumberOfInfluencers() { return numberOfInfluencers; }
    public void setNumberOfInfluencers(Integer numberOfInfluencers) { this.numberOfInfluencers = numberOfInfluencers; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
