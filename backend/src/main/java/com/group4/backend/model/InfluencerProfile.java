package com.group4.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "influencer_profiles")
public class InfluencerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "display_name")
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "instagram_handle")
    private String instagramHandle;

    @Column(name = "tiktok_handle")
    private String tiktokHandle;

    @Column(name = "rate_per_post")
    private Integer ratePerPost;  // in cents or whole currency units

    @Column(name = "rate_per_story")
    private Integer ratePerStory;

    @Column(name = "rate_per_reel")
    private Integer ratePerReel;

    @Column(length = 10)
    private String currency = "USD";

    public InfluencerProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public void setInstagramHandle(String instagramHandle) {
        this.instagramHandle = instagramHandle;
    }

    public String getTiktokHandle() {
        return tiktokHandle;
    }

    public void setTiktokHandle(String tiktokHandle) {
        this.tiktokHandle = tiktokHandle;
    }

    public Integer getRatePerPost() {
        return ratePerPost;
    }

    public void setRatePerPost(Integer ratePerPost) {
        this.ratePerPost = ratePerPost;
    }

    public Integer getRatePerStory() {
        return ratePerStory;
    }

    public void setRatePerStory(Integer ratePerStory) {
        this.ratePerStory = ratePerStory;
    }

    public Integer getRatePerReel() {
        return ratePerReel;
    }

    public void setRatePerReel(Integer ratePerReel) {
        this.ratePerReel = ratePerReel;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
