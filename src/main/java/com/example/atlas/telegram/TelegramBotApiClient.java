package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class TelegramBotApiClient implements TelegramApiClient {

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService;
    private final RestClient.Builder restClientBuilder;

    public TelegramBotApiClient(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    @Override
    public void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }
        restClient()
                .post()
                .uri("/sendMessage")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void sendPhoto(long chatId, String photo, String caption, InlineKeyboardMarkup replyMarkup) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("photo", photo);
        body.put("caption", caption);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }
        restClient()
                .post()
                .uri("/sendPhoto")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void editMessageCaption(long chatId, long messageId, String caption, InlineKeyboardMarkup replyMarkup) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        body.put("caption", caption);
        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }
        restClient()
                .post()
                .uri("/editMessageCaption")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        TelegramGetUpdatesResponse response = restClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getUpdates")
                        .queryParam("offset", Math.max(offset, 0))
                        .queryParam("timeout", Math.max(timeoutSeconds, 0))
                        .queryParam("allowed_updates", "[\"message\",\"callback_query\"]")
                        .build())
                .retrieve()
                .body(TelegramGetUpdatesResponse.class);

        if (response == null || !response.ok()) {
            throw new TelegramApiException("Telegram getUpdates was rejected by Telegram API.");
        }
        return response.result() == null ? List.of() : response.result();
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId, String text) {
        if (callbackQueryId == null || callbackQueryId.isBlank()) {
            return;
        }
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("callback_query_id", callbackQueryId);
        if (text != null && !text.isBlank()) {
            body.put("text", text.strip());
        }
        TelegramApiResponse response = restClient()
                .post()
                .uri("/answerCallbackQuery")
                .body(body)
                .retrieve()
                .body(TelegramApiResponse.class);

        if (response == null || !response.ok()) {
            throw new TelegramApiException("Telegram answerCallbackQuery was rejected by Telegram API.");
        }
    }

    @Override
    public void deleteWebhook(boolean dropPendingUpdates) {
        TelegramApiResponse response = restClient()
                .post()
                .uri("/deleteWebhook")
                .body(Map.of("drop_pending_updates", dropPendingUpdates))
                .retrieve()
                .body(TelegramApiResponse.class);

        if (response == null || !response.ok()) {
            throw new TelegramApiException("Telegram deleteWebhook was rejected by Telegram API.");
        }
    }

    private RestClient restClient() {
        return restClientForToken(effectiveBotToken());
    }

    RestClient restClientForToken(String botToken) {
        if (botToken == null || botToken.isBlank()) {
            throw new TelegramApiException("Telegram bot token is not configured.");
        }
        try {
            return restClientBuilder.clone()
                    .baseUrl("https://api.telegram.org/bot" + botToken.strip())
                    .build();
        } catch (RestClientException exception) {
            throw new TelegramApiException("Telegram API client could not be created.", exception);
        }
    }

    private String effectiveBotToken() {
        AtlasRuntimeSettingsService service = runtimeSettingsService.getIfAvailable();
        if (service != null) {
            EffectiveTelegramConfig config = service.effectiveTelegramConfig();
            if (config.hasBotToken()) {
                return config.botToken();
            }
        }
        return properties.telegram().botToken();
    }

    private record TelegramApiResponse(boolean ok) {
    }

    private record TelegramGetUpdatesResponse(
            boolean ok,
            @JsonProperty("result") List<TelegramUpdate> result
    ) {
    }
}
