package com.group4.backend.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class PasswordResetToken {

    private static final long EXPIRY_DURATION_MS = 1000L * 60 * 60; // 1 hour

    // Requirements: Token, Expiry Date, User mapping
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    // Default constructor
    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = new Date(System.currentTimeMillis() + EXPIRY_DURATION_MS);
    }

    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
