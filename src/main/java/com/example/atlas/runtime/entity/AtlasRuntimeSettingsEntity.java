package com.example.atlas.runtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "atlas_runtime_settings")
public class AtlasRuntimeSettingsEntity {

    @Id
    private UUID id;

    @Column(name = "telegram_bot_token", columnDefinition = "text")
    private String telegramBotToken;

    @Column(name = "telegram_bot_username", length = 255)
    private String telegramBotUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "telegram_mode", length = 32)
    private TelegramLaunchMode telegramMode;

    @Column(name = "telegram_public_base_url", columnDefinition = "text")
    private String telegramPublicBaseUrl;

    @Column(name = "telegram_webhook_secret", columnDefinition = "text")
    private String telegramWebhookSecret;

    @Column(name = "telegram_polling_offset", nullable = false)
    private Long telegramPollingOffset;

    @Column(name = "setup_completed", nullable = false)
    private boolean setupCompleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AtlasRuntimeSettingsEntity() {
    }

    public AtlasRuntimeSettingsEntity(UUID id, Instant now) {
        this.id = id;
        this.telegramPollingOffset = 0L;
        this.setupCompleted = false;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static AtlasRuntimeSettingsEntity create(Instant now) {
        return new AtlasRuntimeSettingsEntity(UUID.randomUUID(), now);
    }

    public void updateTelegramSetup(
            String telegramBotToken,
            String telegramBotUsername,
            TelegramLaunchMode telegramMode,
            String telegramPublicBaseUrl,
            String telegramWebhookSecret,
            Instant updatedAt
    ) {
        this.telegramBotToken = telegramBotToken;
        this.telegramBotUsername = telegramBotUsername;
        this.telegramMode = telegramMode;
        this.telegramPublicBaseUrl = telegramPublicBaseUrl;
        this.telegramWebhookSecret = telegramWebhookSecret;
        this.setupCompleted = true;
        this.updatedAt = updatedAt;
    }

    public void updateTelegramPollingOffset(long telegramPollingOffset, Instant updatedAt) {
        this.telegramPollingOffset = telegramPollingOffset;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public String getTelegramBotUsername() {
        return telegramBotUsername;
    }

    public TelegramLaunchMode getTelegramMode() {
        return telegramMode;
    }

    public String getTelegramPublicBaseUrl() {
        return telegramPublicBaseUrl;
    }

    public String getTelegramWebhookSecret() {
        return telegramWebhookSecret;
    }

    public Long getTelegramPollingOffset() {
        return telegramPollingOffset;
    }

    public boolean isSetupCompleted() {
        return setupCompleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
