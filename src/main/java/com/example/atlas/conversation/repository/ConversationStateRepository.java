package com.example.atlas.conversation.repository;

import com.example.atlas.conversation.ConversationStatus;
import com.example.atlas.conversation.entity.ConversationStateEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationStateRepository extends JpaRepository<ConversationStateEntity, UUID> {

    Optional<ConversationStateEntity> findByTelegramUserAndStatus(TelegramUserEntity telegramUser, ConversationStatus status);

    void deleteByTelegramUser(TelegramUserEntity telegramUser);
}
