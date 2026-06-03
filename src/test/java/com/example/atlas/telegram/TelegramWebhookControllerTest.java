package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TelegramWebhookControllerTest {

    private final TelegramBotAdapter botAdapter = mock(TelegramBotAdapter.class);

    @Test
    void configuredSecretWithValidHeaderIsAccepted() {
        TelegramWebhookController controller = controllerWithSecret("expected-secret");
        TelegramUpdate update = textUpdate("/start");

        ResponseEntity<Void> response = controller.receiveUpdate("expected-secret", update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(botAdapter).handleUpdate(update);
    }

    @Test
    void configuredSecretWithMissingHeaderIsRejected() {
        TelegramWebhookController controller = controllerWithSecret("expected-secret");
        TelegramUpdate update = textUpdate("/start");

        ResponseEntity<Void> response = controller.receiveUpdate(null, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(botAdapter, never()).handleUpdate(update);
    }

    @Test
    void configuredSecretWithInvalidHeaderIsRejected() {
        TelegramWebhookController controller = controllerWithSecret("expected-secret");
        TelegramUpdate update = textUpdate("/start");

        ResponseEntity<Void> response = controller.receiveUpdate("wrong-secret", update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(botAdapter, never()).handleUpdate(update);
    }

    @Test
    void blankConfiguredSecretKeepsBackwardCompatibleRequestHandling() {
        TelegramWebhookController controller = controllerWithSecret("");
        TelegramUpdate update = textUpdate("/start");

        ResponseEntity<Void> response = controller.receiveUpdate(null, update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(botAdapter).handleUpdate(update);
    }

    @Test
    void unsupportedUpdateReturnsOkWithoutCrashing() {
        TelegramWebhookController controller = controllerWithSecret("");

        ResponseEntity<Void> response = controller.receiveUpdate(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(botAdapter).handleUpdate(null);
    }

    private TelegramWebhookController controllerWithSecret(String webhookSecret) {
        return new TelegramWebhookController(
                new AtlasProperties(new AtlasProperties.Telegram(
                        true,
                        "test-token",
                        "atlas_test_bot",
                        "/telegram/webhook",
                        webhookSecret,
                        "",
                        false,
                        true
                )),
                botAdapter
        );
    }

    private TelegramUpdate textUpdate(String text) {
        return new TelegramUpdate(
                100L,
                new TelegramUpdate.TelegramMessage(
                        10L,
                        new TelegramUpdate.TelegramChat(42L),
                        new TelegramUpdate.TelegramUser(7L, "user", "User"),
                        text
                ),
                null,
                null
        );
    }
}
