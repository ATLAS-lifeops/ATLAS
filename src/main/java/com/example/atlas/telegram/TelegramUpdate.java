package com.example.atlas.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUpdate(
        @JsonProperty("update_id") Long updateId,
        TelegramMessage message,
        @JsonProperty("edited_message") TelegramMessage editedMessage,
        @JsonProperty("callback_query") TelegramCallbackQuery callbackQuery
) {

    public record TelegramMessage(
            @JsonProperty("message_id") Long messageId,
            TelegramChat chat,
            TelegramUser from,
            String text
    ) {
    }

    public record TelegramChat(Long id) {
    }

    public record TelegramUser(
            Long id,
            String username,
            @JsonProperty("first_name") String firstName
    ) {
    }

    public record TelegramCallbackQuery(String id) {
    }
}
