package com.example.atlas.planning.repository;

import com.example.atlas.planning.entity.WeeklyFocusEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyFocusRepository extends JpaRepository<WeeklyFocusEntity, UUID> {

    Optional<WeeklyFocusEntity> findByTelegramUserAndWeekStart(TelegramUserEntity telegramUser, LocalDate weekStart);

    java.util.List<WeeklyFocusEntity> findByTelegramUserOrderByWeekStartDesc(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
