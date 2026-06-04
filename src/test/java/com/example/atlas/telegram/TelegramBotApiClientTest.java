package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TelegramBotApiClientTest {

    @Test
    void sendMessageIncludesInlineKeyboardMarkup() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramBotApiClient client = new TelegramBotApiClient(
                properties(),
                noRuntimeSettings(),
                builder
        );

        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chat_id").value(42))
                .andExpect(jsonPath("$.text").value("ATLAS"))
                .andExpect(jsonPath("$.reply_markup.inline_keyboard[0][0].text").value("Меню"))
                .andExpect(jsonPath("$.reply_markup.inline_keyboard[0][0].callback_data").value("atlas:menu"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.sendMessage(42L, "ATLAS", new TelegramKeyboardFactory().backToMenu());

        server.verify();
    }

    @Test
    void sendPhotoIncludesCaptionAndInlineKeyboardMarkup() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramBotApiClient client = new TelegramBotApiClient(properties(), noRuntimeSettings(), builder);

        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/sendPhoto"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chat_id").value(42))
                .andExpect(jsonPath("$.photo").value("https://atlas.example/logo.png"))
                .andExpect(jsonPath("$.caption").value("ATLAS"))
                .andExpect(jsonPath("$.reply_markup.inline_keyboard[0][0].callback_data").value("atlas:menu"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.sendPhoto(42L, "https://atlas.example/logo.png", "ATLAS", new TelegramKeyboardFactory().backToMenu());

        server.verify();
    }

    @Test
    void editMessageCaptionIncludesReplyMarkup() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramBotApiClient client = new TelegramBotApiClient(properties(), noRuntimeSettings(), builder);

        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/editMessageCaption"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chat_id").value(42))
                .andExpect(jsonPath("$.message_id").value(10))
                .andExpect(jsonPath("$.caption").value("ATLAS"))
                .andExpect(jsonPath("$.reply_markup.inline_keyboard[0][0].callback_data").value("atlas:menu"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.editMessageCaption(42L, 10L, "ATLAS", new TelegramKeyboardFactory().backToMenu());

        server.verify();
    }

    @Test
    void deleteMessageSendsTelegramRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramBotApiClient client = new TelegramBotApiClient(properties(), noRuntimeSettings(), builder);

        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/deleteMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.chat_id").value(42))
                .andExpect(jsonPath("$.message_id").value(10))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.deleteMessage(42L, 10L);

        server.verify();
    }

    private AtlasProperties properties() {
        return new AtlasProperties(new AtlasProperties.Telegram(
                true,
                "test-token",
                "atlas_bot",
                "/telegram/webhook",
                "",
                "",
                false,
                true
        ));
    }

    private ObjectProvider<AtlasRuntimeSettingsService> noRuntimeSettings() {
        return new ObjectProvider<>() {
            @Override
            public AtlasRuntimeSettingsService getObject(Object... args) {
                return null;
            }

            @Override
            public AtlasRuntimeSettingsService getIfAvailable() {
                return null;
            }

            @Override
            public AtlasRuntimeSettingsService getIfUnique() {
                return null;
            }

            @Override
            public AtlasRuntimeSettingsService getObject() {
                return null;
            }
        };
    }
}
