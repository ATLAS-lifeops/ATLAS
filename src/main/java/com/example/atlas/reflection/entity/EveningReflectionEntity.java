package com.example.atlas.reflection.entity;

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
@Table(name = "evening_reflections")
public class EveningReflectionEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id")
    private TelegramUserEntity telegramUser;

    @Column(name = "main_result", columnDefinition = "text")
    private String mainResult;

    @Column(name = "main_blocker", columnDefinition = "text")
    private String mainBlocker;

    @Column(name = "tomorrow_focus", columnDefinition = "text")
    private String tomorrowFocus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected EveningReflectionEntity() {
    }

    public EveningReflectionEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            String mainResult,
            String mainBlocker,
            String tomorrowFocus,
            Instant createdAt
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.mainResult = mainResult;
        this.mainBlocker = mainBlocker;
        this.tomorrowFocus = tomorrowFocus;
        this.createdAt = createdAt;
    }

    public static EveningReflectionEntity create(
            TelegramUserEntity telegramUser,
            String mainResult,
            String mainBlocker,
            String tomorrowFocus,
            Instant createdAt
    ) {
        return new EveningReflectionEntity(UUID.randomUUID(), telegramUser, mainResult, mainBlocker, tomorrowFocus, createdAt);
    }

    public String getMainResult() {
        return mainResult;
    }

    public String getMainBlocker() {
        return mainBlocker;
    }

    public String getTomorrowFocus() {
        return tomorrowFocus;
    }
}
