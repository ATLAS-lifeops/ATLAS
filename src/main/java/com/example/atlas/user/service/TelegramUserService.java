package com.example.atlas.user.service;

import com.example.atlas.telegram.TelegramUpdate;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.repository.TelegramUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@ConditionalOnBean(TelegramUserRepository.class)
public class TelegramUserService {

    private final TelegramUserRepository repository;
    private final Clock clock;

    public TelegramUserService(TelegramUserRepository repository) {
        this(repository, Clock.systemUTC());
    }

    TelegramUserService(TelegramUserRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public TelegramUserEntity upsertFromMessage(TelegramUpdate.TelegramMessage message) {
        if (message == null || message.chat() == null || message.chat().id() == null || message.from() == null
                || message.from().id() == null) {
            return null;
        }

        Instant now = Instant.now(clock);
        TelegramUpdate.TelegramUser from = message.from();
        TelegramUserEntity user = repository.findByTelegramUserId(from.id())
                .orElseGet(() -> TelegramUserEntity.create(
                        from.id(),
                        message.chat().id(),
                        stripToNull(from.username()),
                        stripToNull(from.firstName()),
                        now
                ));
        user.updateSeen(message.chat().id(), stripToNull(from.username()), stripToNull(from.firstName()), now);
        return repository.save(user);
    }

    private String stripToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
