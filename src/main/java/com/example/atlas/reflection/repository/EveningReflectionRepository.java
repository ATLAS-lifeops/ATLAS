package com.example.atlas.reflection.repository;

import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EveningReflectionRepository extends JpaRepository<EveningReflectionEntity, UUID> {

    List<EveningReflectionEntity> findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(TelegramUserEntity telegramUser, Instant createdAt);
}
