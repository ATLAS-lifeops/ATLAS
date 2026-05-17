package com.example.atlas.message.entity;

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
@Table(name = "telegram_messages")
public class TelegramMessageEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id")
    private TelegramUserEntity telegramUser;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TelegramMessageDirection direction;

    @Column(name = "request_type", length = 64)
    private String requestType;

    @Column(columnDefinition = "text")
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TelegramMessageEntity() {
    }

    public TelegramMessageEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            Long chatId,
            TelegramMessageDirection direction,
            String requestType,
            String text,
            Instant createdAt
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.chatId = chatId;
        this.direction = direction;
        this.requestType = requestType;
        this.text = text;
        this.createdAt = createdAt;
    }

    public static TelegramMessageEntity create(
            TelegramUserEntity telegramUser,
            Long chatId,
            TelegramMessageDirection direction,
            String requestType,
            String text,
            Instant createdAt
    ) {
        return new TelegramMessageEntity(UUID.randomUUID(), telegramUser, chatId, direction, requestType, text, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public Long getChatId() {
        return chatId;
    }

    public TelegramMessageDirection getDirection() {
        return direction;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
