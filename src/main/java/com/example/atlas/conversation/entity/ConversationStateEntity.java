package com.example.atlas.conversation.entity;

import com.example.atlas.conversation.ConversationFlowType;
import com.example.atlas.conversation.ConversationStatus;
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
@Table(name = "conversation_states")
public class ConversationStateEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false, length = 64)
    private ConversationFlowType flowType;

    @Column(nullable = false, length = 128)
    private String step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConversationStatus status;

    @Column(name = "payload_json", columnDefinition = "text")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ConversationStateEntity() {
    }

    public ConversationStateEntity(
            UUID id,
            TelegramUserEntity telegramUser,
            ConversationFlowType flowType,
            String step,
            String payloadJson,
            Instant now
    ) {
        this.id = id;
        this.telegramUser = telegramUser;
        this.flowType = flowType;
        this.step = step;
        this.status = ConversationStatus.ACTIVE;
        this.payloadJson = payloadJson;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ConversationStateEntity active(
            TelegramUserEntity user,
            ConversationFlowType flowType,
            String step,
            String payloadJson,
            Instant now
    ) {
        return new ConversationStateEntity(UUID.randomUUID(), user, flowType, step, payloadJson, now);
    }

    public void moveTo(String step, String payloadJson, Instant now) {
        this.step = step;
        this.payloadJson = payloadJson;
        this.updatedAt = now;
    }

    public void complete(String payloadJson, Instant now) {
        this.status = ConversationStatus.COMPLETED;
        this.payloadJson = payloadJson;
        this.updatedAt = now;
        this.completedAt = now;
    }

    public void cancel(Instant now) {
        this.status = ConversationStatus.CANCELLED;
        this.updatedAt = now;
        this.completedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public TelegramUserEntity getTelegramUser() {
        return telegramUser;
    }

    public ConversationFlowType getFlowType() {
        return flowType;
    }

    public String getStep() {
        return step;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }
}
