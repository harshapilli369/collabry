package com.group4.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RespondRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "ACCEPT|REJECT", message = "Action must be ACCEPT or REJECT")
    private String action;

    public RespondRequest() {
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
