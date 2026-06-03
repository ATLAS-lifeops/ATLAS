package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

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
        TelegramWebhookApiResponse response;
        try {
            response = restClient.post()
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
