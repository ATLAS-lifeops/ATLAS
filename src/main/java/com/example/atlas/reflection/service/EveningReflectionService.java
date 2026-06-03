package com.example.atlas.reflection.service;

import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.repository.EveningReflectionRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnBean(EveningReflectionRepository.class)
public class EveningReflectionService {

    private final EveningReflectionRepository repository;
    private final Clock clock;

    public EveningReflectionService(EveningReflectionRepository repository) {
        this(repository, Clock.systemUTC());
    }

    EveningReflectionService(EveningReflectionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public EveningReflectionEntity record(TelegramUserEntity user, String mainResult, String mainBlocker, String tomorrowFocus) {
        return repository.save(EveningReflectionEntity.create(
                user,
                trim(mainResult),
                trim(mainBlocker),
                trim(tomorrowFocus),
                Instant.now(clock)
        ));
    }

    @Transactional(readOnly = true)
    public List<EveningReflectionEntity> recent(TelegramUserEntity user, Instant since) {
        return repository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
    }

    private String trim(String value) {
        return value == null ? null : value.strip();
    }
}
