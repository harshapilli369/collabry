package com.group4.backend.dto.campaign;

import com.group4.backend.model.BudgetRange;
import com.group4.backend.model.CampaignGoal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    private String name;

    private String description;

    @NotNull(message = "Budget range is required")
    private BudgetRange budgetRange;

    private CampaignGoal campaignGoal;

    private String preferredContentTypes;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer numberOfInfluencers;

    public CampaignRequest() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BudgetRange getBudgetRange() { return budgetRange; }
    public void setBudgetRange(BudgetRange budgetRange) { this.budgetRange = budgetRange; }

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
}
