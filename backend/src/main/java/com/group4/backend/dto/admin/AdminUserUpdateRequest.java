package com.group4.backend.dto.admin;

import jakarta.validation.constraints.NotNull;

public class AdminUserUpdateRequest {

    @NotNull
    private Boolean active;

    @NotNull
    private Boolean flagged;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }
}
