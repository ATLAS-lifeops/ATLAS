package com.example.atlas.life.service;

import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.repository.LifeProfileRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
@ConditionalOnBean(LifeProfileRepository.class)
public class LifeProfileService {

    private final LifeProfileRepository repository;
    private final Clock clock;

    public LifeProfileService(LifeProfileRepository repository) {
        this(repository, Clock.systemUTC());
    }

    LifeProfileService(LifeProfileRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public LifeProfileEntity getOrCreate(TelegramUserEntity user) {
        return repository.findByTelegramUser(user)
                .orElseGet(() -> repository.save(LifeProfileEntity.create(user, Instant.now(clock))));
    }

    @Transactional(readOnly = true)
    public Optional<LifeProfileEntity> find(TelegramUserEntity user) {
        return repository.findByTelegramUser(user);
    }

    Instant now() {
        return Instant.now(clock);
    }
}
