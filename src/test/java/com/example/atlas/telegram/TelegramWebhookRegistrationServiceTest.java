package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class TelegramWebhookRegistrationServiceTest {

    private final TelegramWebhookClient webhookClient = mock(TelegramWebhookClient.class);

    @Test
    void registrationDisabledDoesNotCallClient() {
        TelegramWebhookRegistrationService service = service(properties(false, "https://atlas.example", "/telegram/webhook"));

        service.registerWebhookIfEnabled();

        verify(webhookClient, never()).setWebhook(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void registrationEnabledWithValidConfigCallsSetWebhook() {
        TelegramWebhookRegistrationService service = service(properties(true, "https://atlas.example/", "/telegram/webhook"));

        service.registerWebhookIfEnabled();

        ArgumentCaptor<TelegramWebhookClient.TelegramWebhookRequest> request =
                ArgumentCaptor.forClass(TelegramWebhookClient.TelegramWebhookRequest.class);
        verify(webhookClient).setWebhook(request.capture());
        assertThat(request.getValue().url()).isEqualTo("https://atlas.example/telegram/webhook");
        assertThat(request.getValue().secretToken()).isEqualTo("webhook-secret");
        assertThat(request.getValue().dropPendingUpdates()).isTrue();
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
        TelegramWebhookClient failingClient = mock(TelegramWebhookClient.class);
        doThrow(new IllegalStateException("Telegram webhook registration was rejected by Telegram API."))
                .when(failingClient)
                .setWebhook(org.mockito.ArgumentMatchers.any());
        TelegramWebhookRegistrationService service = new TelegramWebhookRegistrationService(
                properties(true, "https://atlas.example", "/telegram/webhook"),
                failingClient
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
}
