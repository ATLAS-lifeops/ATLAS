package com.example.atlas.routines.entity;

import com.example.atlas.user.entity.TelegramUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routine_preferences")
public class RoutinePreferencesEntity {

    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Column(name = "checkin_time", nullable = false)
    private String checkinTime;

    @Column(name = "evening_time", nullable = false)
    private String eveningTime;

    @Column(nullable = false)
    private String timezone;

    @Column(name = "quiet_hours_start", nullable = false)
    private String quietHoursStart;

    @Column(name = "quiet_hours_end", nullable = false)
    private String quietHoursEnd;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoutinePreferencesEntity() {
    }

    public RoutinePreferencesEntity(UUID id, TelegramUserEntity telegramUser, String checkinTime, String eveningTime, String timezone, String quietHoursStart, String quietHoursEnd, boolean enabled, Instant updatedAt) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.checkinTime = checkinTime;
        this.eveningTime = eveningTime;
        this.timezone = timezone;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    public static RoutinePreferencesEntity defaults(TelegramUserEntity user, Instant now) {
        return new RoutinePreferencesEntity(UUID.randomUUID(), user, "09:00", "21:00", "Europe/Moscow", "22:00", "08:00", false, now);
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public String getCheckinTime() {
        return checkinTime;
    }

    public String getEveningTime() {
        return eveningTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getQuietHoursStart() {
        return quietHoursStart;
    }

    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
