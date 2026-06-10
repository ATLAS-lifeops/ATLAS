package com.example.atlas.routines.repository;

import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoutinePreferencesRepository extends JpaRepository<RoutinePreferencesEntity, UUID> {

    Optional<RoutinePreferencesEntity> findByTelegramUser(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
