package com.example.atlas.telegram;

import java.util.List;

public interface TelegramApiClient {

    void sendMessage(long chatId, String text);

    default void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        sendMessage(chatId, text);
    }

    default void sendPhoto(long chatId, String photo, String caption, InlineKeyboardMarkup replyMarkup) {
        sendMessage(chatId, caption, replyMarkup);
    }

    default void editMessageCaption(long chatId, long messageId, String caption, InlineKeyboardMarkup replyMarkup) {
        sendMessage(chatId, caption, replyMarkup);
    }

    default void answerCallbackQuery(String callbackQueryId, String text) {
        throw new UnsupportedOperationException("Telegram answerCallbackQuery is not supported by this client.");
    }

    default void deleteMessage(long chatId, long messageId) {
        throw new UnsupportedOperationException("Telegram deleteMessage is not supported by this client.");
    }

    default List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        throw new UnsupportedOperationException("Telegram getUpdates is not supported by this client.");
    }

    default void deleteWebhook(boolean dropPendingUpdates) {
        throw new UnsupportedOperationException("Telegram deleteWebhook is not supported by this client.");
    }
}
