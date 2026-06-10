package com.example.atlas.integrations.entity;

import com.example.atlas.integrations.IntegrationStatus;
import com.example.atlas.integrations.IntegrationType;
import com.example.atlas.user.entity.TelegramUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "integration_settings")
public class IntegrationSettingsEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false)
    private IntegrationType integrationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntegrationStatus status;

    @Column(name = "safe_metadata_json", columnDefinition = "text", nullable = false)
    private String safeMetadataJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected IntegrationSettingsEntity() {
    }

    public IntegrationSettingsEntity(UUID id, TelegramUserEntity telegramUser, IntegrationType integrationType, IntegrationStatus status, String safeMetadataJson, Instant updatedAt) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.integrationType = integrationType;
        this.status = status;
        this.safeMetadataJson = safeMetadataJson;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public String getSafeMetadataJson() {
        return safeMetadataJson;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(IntegrationStatus status, String safeMetadataJson, Instant updatedAt) {
        this.status = status;
        this.safeMetadataJson = safeMetadataJson;
        this.updatedAt = updatedAt;
    }
}
