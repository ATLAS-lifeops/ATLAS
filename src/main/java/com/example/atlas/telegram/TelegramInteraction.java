package com.example.atlas.telegram;

public record TelegramInteraction(
        TelegramInteractionType type,
        Long chatId,
        TelegramUpdate.TelegramUser from,
        String text,
        String callbackQueryId,
        String callbackData
) {
}
