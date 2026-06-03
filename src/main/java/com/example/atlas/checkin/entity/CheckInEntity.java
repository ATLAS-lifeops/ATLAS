package com.example.atlas.checkin.entity;

import com.example.atlas.user.entity.TelegramUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
public class CheckInEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id")
    private TelegramUserEntity telegramUser;

    private Integer energy;

    private Integer fatigue;

    private Integer focus;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    private Integer stress;

    private Integer mood;

    @Column(name = "main_priority", columnDefinition = "text")
    private String mainPriority;

    @Column(name = "overload_flag", nullable = false)
    private boolean overloadFlag;

    @Column(name = "pain_flag", nullable = false)
    private boolean painFlag;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CheckInEntity() {
    }

    public CheckInEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            Integer energy,
            Integer fatigue,
            Integer focus,
            Integer sleepQuality,
            Integer stress,
            Integer mood,
            String mainPriority,
            boolean overloadFlag,
            boolean painFlag,
            String notes,
            Instant createdAt
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.energy = energy;
        this.fatigue = fatigue;
        this.focus = focus;
        this.sleepQuality = sleepQuality;
        this.stress = stress;
        this.mood = mood;
        this.mainPriority = mainPriority;
        this.overloadFlag = overloadFlag;
        this.painFlag = painFlag;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static CheckInEntity create(
            TelegramUserEntity telegramUser,
            Integer energy,
            Integer fatigue,
            Integer focus,
            Integer sleepQuality,
            Integer stress,
            Integer mood,
            String mainPriority,
            boolean overloadFlag,
            boolean painFlag,
            String notes,
            Instant createdAt
    ) {
        return new CheckInEntity(
                UUID.randomUUID(),
                telegramUser,
                energy,
                fatigue,
                focus,
                sleepQuality,
                stress,
                mood,
                mainPriority,
                overloadFlag,
                painFlag,
                notes,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public Integer getEnergy() {
        return energy;
    }

    public Integer getFatigue() {
        return fatigue;
    }

    public Integer getFocus() {
        return focus;
    }

    public Integer getSleepQuality() {
        return sleepQuality;
    }

    public Integer getStress() {
        return stress;
    }

    public Integer getMood() {
        return mood;
    }

    public String getMainPriority() {
        return mainPriority;
    }

    public boolean isOverloadFlag() {
        return overloadFlag;
    }

    public boolean isPainFlag() {
        return painFlag;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
