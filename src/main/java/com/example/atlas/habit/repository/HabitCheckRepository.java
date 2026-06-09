package com.example.atlas.habit.repository;

import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface HabitCheckRepository extends JpaRepository<HabitCheckEntity, UUID> {

    List<HabitCheckEntity> findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(TelegramUserEntity telegramUser, Instant createdAt);

    long countByTelegramUser(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
