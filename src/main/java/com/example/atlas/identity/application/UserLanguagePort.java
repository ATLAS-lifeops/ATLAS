package com.example.atlas.identity.application;

import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.user.UserLanguage;

import java.util.Optional;

public interface UserLanguagePort {

    Optional<UserLanguage> findLanguage(TelegramUserId telegramUserId);

    void setLanguage(TelegramUserId telegramUserId, UserLanguage language);
}
