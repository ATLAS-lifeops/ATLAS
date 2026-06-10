package com.example.atlas.planning.entity;

import com.example.atlas.user.entity.TelegramUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "weekly_focuses")
public class WeeklyFocusEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(columnDefinition = "text", nullable = false)
    private String focus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected WeeklyFocusEntity() {
    }

    public WeeklyFocusEntity(UUID id, TelegramUserEntity telegramUser, LocalDate weekStart, String focus, Instant createdAt) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.weekStart = weekStart;
        this.focus = focus;
        this.createdAt = createdAt;
    }

    public String getFocus() {
        return focus;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void updateFocus(String focus, Instant now) {
        this.focus = focus;
        this.createdAt = now;
    }
}
