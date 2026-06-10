package com.example.atlas.routines;

import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import com.example.atlas.routines.repository.RoutinePreferencesRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@ConditionalOnBean(RoutinePreferencesRepository.class)
public class RoutinePreferencesService {

    private final RoutinePreferencesRepository repository;

    public RoutinePreferencesService(RoutinePreferencesRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RoutinePreferencesEntity getOrCreate(TelegramUserEntity user) {
        return repository.findByTelegramUser(user).orElseGet(() -> repository.save(RoutinePreferencesEntity.defaults(user, Instant.now())));
    }
}
