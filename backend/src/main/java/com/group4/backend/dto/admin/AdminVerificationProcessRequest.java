package com.group4.backend.dto.admin;

import jakarta.validation.constraints.NotNull;

public class AdminVerificationProcessRequest {

    @NotNull
    private Boolean approved;
    private String reason;

    public AdminVerificationProcessRequest() {}

    public AdminVerificationProcessRequest(Boolean approved, String reason) {
        this.approved = approved;
        this.reason = reason;
    }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
