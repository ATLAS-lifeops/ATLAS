package com.example.atlas.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telegram_users")
public class TelegramUserEntity {

    @Id
    private UUID id;

    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(length = 255)
    private String username;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    protected TelegramUserEntity() {
    }

    public TelegramUserEntity(
            UUID id,
            Long telegramUserId,
            Long chatId,
            String username,
            String firstName,
            Instant createdAt,
            Instant lastSeenAt
    ) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.chatId = chatId;
        this.username = username;
        this.firstName = firstName;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
    }

    public static TelegramUserEntity create(
            Long telegramUserId,
            Long chatId,
            String username,
            String firstName,
            Instant now
    ) {
        return new TelegramUserEntity(UUID.randomUUID(), telegramUserId, chatId, username, firstName, now, now);
    }

    public void updateSeen(Long chatId, String username, String firstName, Instant lastSeenAt) {
        this.chatId = chatId;
        this.username = username;
        this.firstName = firstName;
        this.lastSeenAt = lastSeenAt;
    }

    public UUID getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }
}
