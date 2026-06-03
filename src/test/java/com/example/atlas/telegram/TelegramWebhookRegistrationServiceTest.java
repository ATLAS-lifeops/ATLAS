package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramWebhookRegistrationServiceTest {

    private final RecordingTelegramWebhookClient webhookClient = new RecordingTelegramWebhookClient();

    @Test
    void registrationDisabledDoesNotCallClient() {
        TelegramWebhookRegistrationService service = service(properties(false, "https://atlas.example", "/telegram/webhook"));

        service.registerWebhookIfEnabled();

        assertThat(webhookClient.request()).isNull();
    }

    @Test
    void registrationEnabledWithValidConfigCallsSetWebhook() {
        TelegramWebhookRegistrationService service = service(properties(true, "https://atlas.example/", "/telegram/webhook"));

        service.registerWebhookIfEnabled();

        assertThat(webhookClient.request().url()).isEqualTo("https://atlas.example/telegram/webhook");
        assertThat(webhookClient.request().secretToken()).isEqualTo("webhook-secret");
        assertThat(webhookClient.request().dropPendingUpdates()).isTrue();
    }

    @Test
    void registrationEnabledWithoutPublicBaseUrlFailsFast() {
        TelegramWebhookRegistrationService service = service(properties(true, "", "/telegram/webhook"));

        assertThatThrownBy(service::registerWebhookIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ATLAS_PUBLIC_BASE_URL");
    }

    @Test
    void registrationEnabledWithNonHttpsPublicBaseUrlFailsFast() {
        TelegramWebhookRegistrationService service = service(properties(true, "http://atlas.example", "/telegram/webhook"));

        assertThatThrownBy(service::registerWebhookIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("https://");
    }

    @Test
    void registrationEnabledWithInvalidWebhookPathFailsFast() {
        TelegramWebhookRegistrationService service = service(properties(true, "https://atlas.example", "telegram/webhook"));

        assertThatThrownBy(service::registerWebhookIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("start with /");
    }

    @Test
    void telegramApiFailurePropagatesClearException() {
        TelegramWebhookRegistrationService service = new TelegramWebhookRegistrationService(
                properties(true, "https://atlas.example", "/telegram/webhook"),
                new FailingTelegramWebhookClient()
        );

        assertThatThrownBy(service::registerWebhookIfEnabled)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Telegram webhook registration");
    }

    private TelegramWebhookRegistrationService service(AtlasProperties properties) {
        return new TelegramWebhookRegistrationService(properties, webhookClient);
    }

    private AtlasProperties properties(boolean registerWebhook, String publicBaseUrl, String webhookPath) {
        return new AtlasProperties(new AtlasProperties.Telegram(
                true,
                "test-token",
                "atlas_test_bot",
                webhookPath,
                "webhook-secret",
                publicBaseUrl,
                registerWebhook,
                true
        ));
    }

    private static class RecordingTelegramWebhookClient extends TelegramWebhookClient {

        private TelegramWebhookRequest request;

        RecordingTelegramWebhookClient() {
            super(RestClient.builder().baseUrl("https://api.telegram.org/bottest-token").build());
        }

        @Override
        public void setWebhook(TelegramWebhookRequest request) {
            this.request = request;
        }

        TelegramWebhookRequest request() {
            return request;
        }
    }

    private static class FailingTelegramWebhookClient extends TelegramWebhookClient {

        FailingTelegramWebhookClient() {
            super(RestClient.builder().baseUrl("https://api.telegram.org/bottest-token").build());
        }

        @Override
        public void setWebhook(TelegramWebhookRequest request) {
            throw new IllegalStateException("Telegram webhook registration was rejected by Telegram API.");
        }
    }
}
