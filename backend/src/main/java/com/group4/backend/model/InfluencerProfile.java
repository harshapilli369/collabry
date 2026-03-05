package com.group4.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "influencer_profiles", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class InfluencerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String niche;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "instagram_handle")
    private String instagramHandle;

    @Column(name = "youtube_handle")
    private String youtubeHandle;

    @Column(name = "tiktok_handle")
    private String tiktokHandle;

    @Column(precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "audience_info", columnDefinition = "TEXT")
    private String audienceInfo;

    @Column(name = "is_complete", nullable = false)
    private boolean isComplete = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null)
            createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public InfluencerProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNiche() {
        return niche;
    }

    public void setNiche(String niche) {
        this.niche = niche;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public void setInstagramHandle(String instagramHandle) {
        this.instagramHandle = instagramHandle;
    }

    public String getYoutubeHandle() {
        return youtubeHandle;
    }

    public void setYoutubeHandle(String youtubeHandle) {
        this.youtubeHandle = youtubeHandle;
    }

    public String getTiktokHandle() {
        return tiktokHandle;
    }

    public void setTiktokHandle(String tiktokHandle) {
        this.tiktokHandle = tiktokHandle;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getAudienceInfo() {
        return audienceInfo;
    }

    public void setAudienceInfo(String audienceInfo) {
        this.audienceInfo = audienceInfo;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
