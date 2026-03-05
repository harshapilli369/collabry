package com.group4.backend.dto;

import com.group4.backend.model.Role;

public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private Long id;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, Role role, Long id) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
