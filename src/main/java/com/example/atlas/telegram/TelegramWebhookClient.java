package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramWebhookClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookClient.class);

    private final RestClient restClient;

    @Autowired
    public TelegramWebhookClient(AtlasProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.telegram.org/bot" + properties.telegram().botToken())
                .build();
    }

    TelegramWebhookClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void setWebhook(TelegramWebhookRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("url", request.url());
        if (request.hasSecretToken()) {
            body.put("secret_token", request.secretToken());
        }
        body.put("drop_pending_updates", request.dropPendingUpdates());
        body.put("allowed_updates", List.of("message"));

        TelegramWebhookApiResponse response;
        try {
            response = restClient.post()
                    .uri("/setWebhook")
                    .body(body)
                    .retrieve()
                    .body(TelegramWebhookApiResponse.class);
        } catch (RestClientException exception) {
            log.warn("Telegram setWebhook request failed with {}", exception.getClass().getSimpleName());
            throw new IllegalStateException("Telegram webhook registration request failed.", exception);
        }

        if (response == null || !response.ok()) {
            log.warn("Telegram setWebhook was rejected by Telegram API");
            throw new IllegalStateException("Telegram webhook registration was rejected by Telegram API.");
        }

        log.info("Telegram setWebhook accepted by Telegram API");
    }

    public record TelegramWebhookRequest(
            String url,
            String secretToken,
            boolean dropPendingUpdates
    ) {
        boolean hasSecretToken() {
            return secretToken != null && !secretToken.isBlank();
        }
    }

    private record TelegramWebhookApiResponse(
            boolean ok,
            @JsonProperty("description") String description
    ) {
    }
}
