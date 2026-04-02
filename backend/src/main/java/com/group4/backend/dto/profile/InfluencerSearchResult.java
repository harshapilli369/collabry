package com.group4.backend.dto.profile;

/** Minimal influencer info for brand invite (id, email, display name). */
public class InfluencerSearchResult {

    private Long id;
    private String email;
    private String displayName;

    public InfluencerSearchResult() {
    }

    public InfluencerSearchResult(Long id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName != null ? displayName : email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
