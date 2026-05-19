package com.example.atlas.setup.service;

import org.springframework.stereotype.Service;

@Service
public class TelegramBotTokenValidator {

    public TelegramBotIdentity validate(String botToken) {
        if (botToken == null || botToken.isBlank()) {
            throw new TelegramBotTokenValidationException("Telegram Bot Token is required.");
        }
        return new TelegramBotIdentity(0L, null, null);
    }
}
