package com.example.atlas.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramUpdateParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void messageUpdateStillParses() throws Exception {
        TelegramUpdate update = objectMapper.readValue("""
                {
                  "update_id": 100,
                  "message": {
                    "message_id": 10,
                    "chat": { "id": 42 },
                    "from": { "id": 7, "username": "user", "first_name": "User" },
                    "text": "/start"
                  }
                }
                """, TelegramUpdate.class);

        assertThat(update.updateId()).isEqualTo(100L);
        assertThat(update.message().chat().id()).isEqualTo(42L);
        assertThat(update.message().text()).isEqualTo("/start");
        assertThat(update.callbackQuery()).isNull();
    }

    @Test
    void callbackQueryUpdateParses() throws Exception {
        TelegramUpdate update = objectMapper.readValue("""
                {
                  "update_id": 101,
                  "callback_query": {
                    "id": "callback-1",
                    "from": { "id": 7, "username": "user", "first_name": "User" },
                    "message": {
                      "message_id": 11,
                      "chat": { "id": 42 },
                      "from": { "id": 8, "username": "atlas_bot", "first_name": "ATLAS" }
                    },
                    "data": "atlas:menu"
                  }
                }
                """, TelegramUpdate.class);

        assertThat(update.updateId()).isEqualTo(101L);
        assertThat(update.message()).isNull();
        assertThat(update.callbackQuery().id()).isEqualTo("callback-1");
        assertThat(update.callbackQuery().from().id()).isEqualTo(7L);
        assertThat(update.callbackQuery().message().chat().id()).isEqualTo(42L);
        assertThat(update.callbackQuery().data()).isEqualTo("atlas:menu");
    }
}
