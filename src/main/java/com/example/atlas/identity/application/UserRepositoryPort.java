package com.example.atlas.identity.application;

import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.user.entity.TelegramUserEntity;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<TelegramUserEntity> findByTelegramUserId(TelegramUserId telegramUserId);

    TelegramUserEntity save(TelegramUserEntity user);
}
