package com.example.atlas.conversation.service;

import com.example.atlas.conversation.ConversationFlowType;
import com.example.atlas.conversation.ConversationStatus;
import com.example.atlas.conversation.entity.ConversationStateEntity;
import com.example.atlas.conversation.repository.ConversationStateRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnBean(ConversationStateRepository.class)
public class ConversationStateService {

    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {
    };

    private final ConversationStateRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ConversationStateService(ConversationStateRepository repository, ObjectMapper objectMapper) {
        this(repository, objectMapper, Clock.systemUTC());
    }

    ConversationStateService(ConversationStateRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ConversationStateEntity start(TelegramUserEntity user, ConversationFlowType flowType, String step) {
        active(user).ifPresent(this::cancel);
        return repository.save(ConversationStateEntity.active(user, flowType, step, "{}", now()));
    }

    @Transactional(readOnly = true)
    public Optional<ConversationStateEntity> active(TelegramUserEntity user) {
        return repository.findByTelegramUserAndStatus(user, ConversationStatus.ACTIVE);
    }

    @Transactional
    public void moveTo(ConversationStateEntity state, String step, Map<String, String> payload) {
        state.moveTo(step, writePayload(payload), now());
        repository.save(state);
    }

    @Transactional
    public void complete(ConversationStateEntity state, Map<String, String> payload) {
        state.complete(writePayload(payload), now());
        repository.save(state);
    }

    @Transactional
    public void cancel(ConversationStateEntity state) {
        state.cancel(now());
        repository.save(state);
    }

    public Map<String, String> payload(ConversationStateEntity state) {
        if (state == null || state.getPayloadJson() == null || state.getPayloadJson().isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return new LinkedHashMap<>(objectMapper.readValue(state.getPayloadJson(), STRING_MAP));
        } catch (Exception exception) {
            return new LinkedHashMap<>();
        }
    }

    private String writePayload(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception exception) {
            return "{}";
        }
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
