package com.group4.backend.dto.admin;

import com.group4.backend.model.Role;
import com.group4.backend.model.VerificationRequestStatus;
import java.time.Instant;

public class AdminVerificationRequestDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private Role userRole;
    private VerificationRequestStatus status;
    private String adminReason;
    private Instant createdAt;
    private Instant updatedAt;

    public AdminVerificationRequestDto() {}

    public AdminVerificationRequestDto(Long id, Long userId, String userEmail, Role userRole, 
                                      VerificationRequestStatus status, String adminReason, 
                                      Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.status = status;
        this.adminReason = adminReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Role getUserRole() { return userRole; }
    public void setUserRole(Role userRole) { this.userRole = userRole; }
    public VerificationRequestStatus getStatus() { return status; }
    public void setStatus(VerificationRequestStatus status) { this.status = status; }
    public String getAdminReason() { return adminReason; }
    public void setAdminReason(String adminReason) { this.adminReason = adminReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
