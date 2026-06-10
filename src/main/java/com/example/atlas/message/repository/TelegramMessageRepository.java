package com.example.atlas.message.repository;

import com.example.atlas.message.entity.TelegramMessageEntity;
import com.example.atlas.message.entity.TelegramMessageDirection;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessageEntity, UUID> {

    List<TelegramMessageEntity> findByTelegramUserOrderByCreatedAtDesc(TelegramUserEntity telegramUser);

    long countByTelegramUserAndCreatedAtAfter(TelegramUserEntity telegramUser, Instant createdAt);

    long countByDirectionAndCreatedAtAfter(TelegramMessageDirection direction, Instant createdAt);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
