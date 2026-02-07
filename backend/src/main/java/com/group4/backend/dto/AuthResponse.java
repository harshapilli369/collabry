package com.group4.backend.dto;

import com.group4.backend.model.Role;

public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private String displayName;
    private String companyName;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, Role role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String token, String email, Role role, String displayName, String companyName) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
        this.companyName = companyName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
