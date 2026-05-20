package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class TelegramWebhookClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookClient.class);

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService;
    private final RestClient.Builder restClientBuilder;
    private final RestClient restClient;

    @Autowired
    public TelegramWebhookClient(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.restClientBuilder = restClientBuilder;
        this.restClient = null;
    }

    TelegramWebhookClient(RestClient restClient) {
        this.properties = null;
        this.runtimeSettingsService = null;
        this.restClientBuilder = null;
        this.restClient = restClient;
    }

    public void setWebhook(TelegramWebhookRequest request) {
        setWebhook(effectiveBotToken(), request);
    }

    public void setWebhook(String botToken, TelegramWebhookRequest request) {
        TelegramWebhookApiResponse response;
        try {
            response = restClient(botToken)
                    .post()
                    .uri("/setWebhook")
                    .body(TelegramWebhookPayload.from(request))
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

    private RestClient restClient(String botToken) {
        if (restClient != null) {
            return restClient;
        }
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("Telegram bot token is not configured.");
        }
        return restClientBuilder.clone()
                .baseUrl("https://api.telegram.org/bot" + botToken.strip())
                .build();
    }

    private String effectiveBotToken() {
        AtlasRuntimeSettingsService service = runtimeSettingsService == null ? null : runtimeSettingsService.getIfAvailable();
        if (service != null) {
            EffectiveTelegramConfig config = service.effectiveTelegramConfig();
            if (config.hasBotToken()) {
                return config.botToken();
            }
        }
        if (properties == null) {
            return "";
        }
        return properties.telegram().botToken();
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static final class TelegramWebhookPayload {

        private final String url;
        private final String secretToken;
        private final boolean dropPendingUpdates;
        private final List<String> allowedUpdates;

        private TelegramWebhookPayload(
                String url,
                String secretToken,
                boolean dropPendingUpdates,
                List<String> allowedUpdates
        ) {
            this.url = url;
            this.secretToken = secretToken;
            this.dropPendingUpdates = dropPendingUpdates;
            this.allowedUpdates = allowedUpdates;
        }

        static TelegramWebhookPayload from(TelegramWebhookRequest request) {
            return new TelegramWebhookPayload(
                    request.url(),
                    request.hasSecretToken() ? request.secretToken() : null,
                    request.dropPendingUpdates(),
                    List.of("message")
            );
        }

        public String getUrl() {
            return url;
        }

        @JsonProperty("secret_token")
        public String getSecretToken() {
            return secretToken;
        }

        @JsonProperty("drop_pending_updates")
        public boolean isDropPendingUpdates() {
            return dropPendingUpdates;
        }

        @JsonProperty("allowed_updates")
        public List<String> getAllowedUpdates() {
            return allowedUpdates;
        }

        @Override
        public String toString() {
            return "TelegramWebhookPayload{"
                    + "url='" + url + '\''
                    + ", secret_token=" + (secretToken == null ? "absent" : "configured")
                    + ", drop_pending_updates=" + dropPendingUpdates
                    + ", allowed_updates=" + allowedUpdates
                    + '}';
        }
    }
}
