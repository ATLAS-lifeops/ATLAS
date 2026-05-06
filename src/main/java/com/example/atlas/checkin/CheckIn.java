package com.example.atlas.checkin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "checkins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Integer energy;

    private Integer fatigue;

    @Column(length = 500)
    private String notes;

    private Instant createdAt;

    protected CheckIn() {
    }

    public CheckIn(Long userId, Integer energy, Integer fatigue, String notes, Instant createdAt) {
        this.userId = userId;
        this.energy = energy;
        this.fatigue = fatigue;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getEnergy() {
        return energy;
    }

    public Integer getFatigue() {
        return fatigue;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
