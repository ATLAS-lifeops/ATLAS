package com.example.atlas.checkin.repository;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckInEntity, UUID> {

    List<CheckInEntity> findByTelegramUserOrderByCreatedAtDesc(TelegramUserEntity telegramUser);

    List<CheckInEntity> findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(TelegramUserEntity telegramUser, Instant createdAt);

    long countByTelegramUserAndCreatedAtAfter(TelegramUserEntity telegramUser, Instant createdAt);

    long countByTelegramUser(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);

    long countByCreatedAtAfter(Instant createdAt);

    List<CheckInEntity> findTop20ByOrderByCreatedAtDesc();
}
