package com.example.atlas.reporting.entity;

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
@Table(name = "report_archives")
public class ReportArchiveEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReportArchiveEntity() {
    }

    public ReportArchiveEntity(UUID id, TelegramUserEntity telegramUser, LocalDate weekStart, String content, Instant createdAt) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.weekStart = weekStart;
        this.content = content;
        this.createdAt = createdAt;
    }
}
