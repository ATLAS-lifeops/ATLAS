package com.example.atlas.user.repository;

import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUserEntity, UUID> {

    Optional<TelegramUserEntity> findByTelegramUserId(Long telegramUserId);
}
