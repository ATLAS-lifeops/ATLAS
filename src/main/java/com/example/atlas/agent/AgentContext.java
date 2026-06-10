package com.example.atlas.agent;

import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.user.entity.TelegramUserEntity;

import java.time.Instant;
import java.util.UUID;

public record AgentContext(
        Long telegramUserId,
        UUID internalUserId,
        TelegramUserEntity user,
        String message,
        RequestType requestType,
        Instant receivedAt
) {

    public AgentContext(Long telegramUserId, String message, RequestType requestType, Instant receivedAt) {
        this(telegramUserId, null, null, message, requestType, receivedAt);
    }

    public static AgentContext anonymous(String message, RequestType requestType) {
        return new AgentContext(null, null, null, message, requestType, Instant.now());
    }

    public static AgentContext forUser(TelegramUserEntity user, String message, RequestType requestType) {
        return new AgentContext(
                user == null ? null : user.getTelegramUserId(),
                user == null ? null : user.getId(),
                user,
                message,
                requestType,
                Instant.now()
        );
    }
}
