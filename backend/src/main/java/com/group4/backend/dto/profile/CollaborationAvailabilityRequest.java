package com.group4.backend.dto.profile;

import jakarta.validation.constraints.NotNull;

/**
 * Body for updating whether an influencer is open to new brand collaborations.
 */
public class CollaborationAvailabilityRequest {

    @NotNull(message = "openToCollaborations is required")
    private Boolean openToCollaborations;

    public Boolean getOpenToCollaborations() {
        return openToCollaborations;
    }

    public void setOpenToCollaborations(Boolean openToCollaborations) {
        this.openToCollaborations = openToCollaborations;
    }
}
