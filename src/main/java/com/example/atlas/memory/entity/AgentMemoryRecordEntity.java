package com.example.atlas.memory.entity;

import com.example.atlas.agent.AgentType;
import com.example.atlas.memory.MemoryConfidence;
import com.example.atlas.memory.MemoryScope;
import com.example.atlas.memory.MemorySource;
import com.example.atlas.memory.MemoryType;
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
@Table(name = "agent_memory_records")
public class AgentMemoryRecordEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Column(name = "internal_user_id", nullable = false)
    private UUID internalUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    private AgentType agentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_type", nullable = false)
    private MemoryType memoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_scope", nullable = false)
    private MemoryScope memoryScope;

    @Column(columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryConfidence confidence;

    @Column(columnDefinition = "text")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemorySource source;

    @Column(name = "deduplication_key", nullable = false)
    private String deduplicationKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean archived;

    protected AgentMemoryRecordEntity() {
    }

    public AgentMemoryRecordEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            AgentType agentType,
            MemoryType memoryType,
            MemoryScope memoryScope,
            String title,
            String content,
            MemoryConfidence confidence,
            String tags,
            MemorySource source,
            String deduplicationKey,
            Instant createdAt,
            Instant updatedAt,
            Instant expiresAt,
            boolean archived
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.internalUserId = telegramUser.getId();
        this.agentType = agentType;
        this.memoryType = memoryType;
        this.memoryScope = memoryScope;
        this.title = title;
        this.content = content;
        this.confidence = confidence;
        this.tags = tags;
        this.source = source;
        this.deduplicationKey = deduplicationKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.archived = archived;
    }

    public void archive(Instant now) {
        this.archived = true;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public UUID getInternalUserId() {
        return internalUserId;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public MemoryScope getMemoryScope() {
        return memoryScope;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public MemoryConfidence getConfidence() {
        return confidence;
    }

    public String getTags() {
        return tags;
    }

    public MemorySource getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isArchived() {
        return archived;
    }
}
