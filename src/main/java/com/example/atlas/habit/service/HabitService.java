package com.example.atlas.habit.service;

import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.repository.HabitCheckRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnBean(HabitCheckRepository.class)
public class HabitService {

    private final HabitCheckRepository repository;
    private final Clock clock;

    public HabitService(HabitCheckRepository repository) {
        this(repository, Clock.systemUTC());
    }

    HabitService(HabitCheckRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public HabitCheckEntity record(TelegramUserEntity user, String habitName, String minimumVersion, boolean completed, String notes) {
        return repository.save(HabitCheckEntity.create(
                user,
                trim(habitName),
                trim(minimumVersion),
                completed,
                trim(notes),
                Instant.now(clock)
        ));
    }

    @Transactional(readOnly = true)
    public List<HabitCheckEntity> recent(TelegramUserEntity user, Instant since) {
        return repository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
    }

    private String trim(String value) {
        return value == null ? null : value.strip();
    }
}
