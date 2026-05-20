package com.example.atlas.telegram;

import java.util.List;

public interface TelegramApiClient {

    void sendMessage(long chatId, String text);

    default List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        throw new UnsupportedOperationException("Telegram getUpdates is not supported by this client.");
    }

    default void deleteWebhook(boolean dropPendingUpdates) {
        throw new UnsupportedOperationException("Telegram deleteWebhook is not supported by this client.");
    }
}
