package com.example.atlas.message.service;

import com.example.atlas.message.entity.TelegramMessageDirection;
import com.example.atlas.message.entity.TelegramMessageEntity;
import com.example.atlas.message.repository.TelegramMessageRepository;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@ConditionalOnBean(TelegramMessageRepository.class)
public class TelegramMessagePersistenceService {

    private final TelegramMessageRepository repository;
    private final Clock clock;

    public TelegramMessagePersistenceService(TelegramMessageRepository repository) {
        this(repository, Clock.systemUTC());
    }

    TelegramMessagePersistenceService(TelegramMessageRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public TelegramMessageEntity recordIncoming(
            TelegramUserEntity user,
            Long chatId,
            RequestType requestType,
            String text
    ) {
        return record(user, chatId, TelegramMessageDirection.INBOUND, requestType, text);
    }

    @Transactional
    public TelegramMessageEntity recordOutgoing(
            TelegramUserEntity user,
            Long chatId,
            RequestType requestType,
            String text
    ) {
        return record(user, chatId, TelegramMessageDirection.OUTBOUND, requestType, text);
    }

    private TelegramMessageEntity record(
            TelegramUserEntity user,
            Long chatId,
            TelegramMessageDirection direction,
            RequestType requestType,
            String text
    ) {
        return repository.save(TelegramMessageEntity.create(
                user,
                chatId,
                direction,
                requestType == null ? null : requestType.name(),
                text,
                Instant.now(clock)
        ));
    }
}
