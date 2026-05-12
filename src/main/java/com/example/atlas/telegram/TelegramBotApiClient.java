package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramBotApiClient implements TelegramApiClient {

    private final RestClient restClient;

    public TelegramBotApiClient(AtlasProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.telegram.org/bot" + properties.telegram().botToken())
                .build();
    }

    @Override
    public void sendMessage(long chatId, String text) {
        restClient.post()
                .uri("/sendMessage")
                .body(Map.of(
                        "chat_id", chatId,
                        "text", text
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
