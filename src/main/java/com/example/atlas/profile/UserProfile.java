package com.example.atlas.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long userId;

    @Column(length = 64)
    private String goal;

    @Column(length = 64)
    private String timezone;

    private Instant createdAt;

    protected UserProfile() {
    }

    public UserProfile(Long userId, String goal, String timezone, Instant createdAt) {
        this.userId = userId;
        this.goal = goal;
        this.timezone = timezone;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getGoal() {
        return goal;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
