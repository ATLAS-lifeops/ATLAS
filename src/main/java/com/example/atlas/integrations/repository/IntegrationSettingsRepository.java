package com.example.atlas.integrations.repository;

import com.example.atlas.integrations.IntegrationType;
import com.example.atlas.integrations.entity.IntegrationSettingsEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationSettingsRepository extends JpaRepository<IntegrationSettingsEntity, UUID> {

    Optional<IntegrationSettingsEntity> findByTelegramUserAndIntegrationType(TelegramUserEntity telegramUser, IntegrationType integrationType);

    List<IntegrationSettingsEntity> findByTelegramUserOrderByIntegrationTypeAsc(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
