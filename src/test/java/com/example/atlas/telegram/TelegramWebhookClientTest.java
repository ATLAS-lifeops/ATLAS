package com.example.atlas.telegram;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TelegramWebhookClientTest {

    @Test
    void setWebhookIncludesAllowedUpdatesAndSecretTokenWhenConfigured() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramWebhookClient client = new TelegramWebhookClient(
                builder.baseUrl("https://api.telegram.org/bottest-token").build()
        );
        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/setWebhook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("https://atlas.example/telegram/webhook"))
                .andExpect(jsonPath("$.secret_token").value("webhook-secret"))
                .andExpect(jsonPath("$.drop_pending_updates").value(true))
                .andExpect(jsonPath("$.allowed_updates[0]").value("message"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.setWebhook(new TelegramWebhookClient.TelegramWebhookRequest(
                "https://atlas.example/telegram/webhook",
                "webhook-secret",
                true
        ));

        server.verify();
    }

    @Test
    void setWebhookOmitsSecretTokenWhenBlank() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramWebhookClient client = new TelegramWebhookClient(
                builder.baseUrl("https://api.telegram.org/bottest-token").build()
        );
        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/setWebhook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.secret_token").doesNotExist())
                .andExpect(jsonPath("$.allowed_updates[0]").value("message"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        client.setWebhook(new TelegramWebhookClient.TelegramWebhookRequest(
                "https://atlas.example/telegram/webhook",
                "",
                true
        ));

        server.verify();
    }

    @Test
    void setWebhookRejectsFailedTelegramApiResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TelegramWebhookClient client = new TelegramWebhookClient(
                builder.baseUrl("https://api.telegram.org/bottest-token").build()
        );
        server.expect(once(), requestTo("https://api.telegram.org/bottest-token/setWebhook"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"ok\":false,\"description\":\"bad webhook\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.setWebhook(new TelegramWebhookClient.TelegramWebhookRequest(
                "https://atlas.example/telegram/webhook",
                "",
                true
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rejected");

        server.verify();
    }
}
