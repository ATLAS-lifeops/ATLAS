package com.example.atlas.habit.entity;

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
@Table(name = "habit_checks")
public class HabitCheckEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id")
    private TelegramUserEntity telegramUser;

    @Column(name = "habit_name", nullable = false, columnDefinition = "text")
    private String habitName;

    @Column(name = "minimum_version", columnDefinition = "text")
    private String minimumVersion;

    @Column(nullable = false)
    private boolean completed;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HabitCheckEntity() {
    }

    public HabitCheckEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            String habitName,
            String minimumVersion,
            boolean completed,
            String notes,
            Instant createdAt
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.habitName = habitName;
        this.minimumVersion = minimumVersion;
        this.completed = completed;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static HabitCheckEntity create(
            TelegramUserEntity telegramUser,
            String habitName,
            String minimumVersion,
            boolean completed,
            String notes,
            Instant createdAt
    ) {
        return new HabitCheckEntity(UUID.randomUUID(), telegramUser, habitName, minimumVersion, completed, notes, createdAt);
    }

    public String getHabitName() {
        return habitName;
    }

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public boolean isCompleted() {
        return completed;
    }
}
