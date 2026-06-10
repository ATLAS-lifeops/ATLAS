package com.example.atlas.life.repository;

import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LifeProfileRepository extends JpaRepository<LifeProfileEntity, UUID> {

    Optional<LifeProfileEntity> findByTelegramUser(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
