package com.example.atlas.identity.domain;

import com.example.atlas.shared.domain.TelegramUserId;

public record UserIdentity(TelegramUserId telegramUserId, String username) {
}
