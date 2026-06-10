package com.example.atlas.reporting.repository;

import com.example.atlas.reporting.entity.ReportArchiveEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportArchiveRepository extends JpaRepository<ReportArchiveEntity, UUID> {

    List<ReportArchiveEntity> findByTelegramUserOrderByCreatedAtDesc(TelegramUserEntity telegramUser);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
