package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramWebhookController {

    static final String SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final AtlasProperties properties;
    private final TelegramBotAdapter botAdapter;

    public TelegramWebhookController(AtlasProperties properties, TelegramBotAdapter botAdapter) {
        this.properties = properties;
        this.botAdapter = botAdapter;
    }

    @PostMapping("${atlas.telegram.webhook-path:/telegram/webhook}")
    public ResponseEntity<Void> receiveUpdate(
            @RequestHeader(name = SECRET_TOKEN_HEADER, required = false) String secretToken,
            @RequestBody(required = false) TelegramUpdate update
    ) {
        if (!hasValidSecretToken(secretToken)) {
            return ResponseEntity.status(403).build();
        }

        botAdapter.handleUpdate(update);
        return ResponseEntity.ok().build();
    }

    private boolean hasValidSecretToken(String secretToken) {
        if (!properties.telegram().hasWebhookSecret()) {
            return true;
        }

        if (secretToken == null || secretToken.isBlank()) {
            return false;
        }

        byte[] expected = properties.telegram().webhookSecret().getBytes(StandardCharsets.UTF_8);
        byte[] actual = secretToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
