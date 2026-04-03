package com.group4.backend.dto;

import com.group4.backend.model.VerificationRequestStatus;
import java.time.Instant;

public class VerificationStatusResponse {
    private VerificationRequestStatus status;
    private String adminReason;
    private Instant createdAt;
    private Instant updatedAt;

    public VerificationStatusResponse() {}

    public VerificationStatusResponse(VerificationRequestStatus status, String adminReason, Instant createdAt, Instant updatedAt) {
        this.status = status;
        this.adminReason = adminReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public VerificationRequestStatus getStatus() { return status; }
    public void setStatus(VerificationRequestStatus status) { this.status = status; }
    public String getAdminReason() { return adminReason; }
    public void setAdminReason(String adminReason) { this.adminReason = adminReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
