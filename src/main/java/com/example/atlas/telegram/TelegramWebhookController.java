package com.example.atlas.telegram;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.EffectiveTelegramConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
public class TelegramWebhookController {

    static final String SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final AtlasProperties properties;
    private final ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService;
    private final TelegramBotAdapter botAdapter;

    @Autowired
    public TelegramWebhookController(
            AtlasProperties properties,
            ObjectProvider<AtlasRuntimeSettingsService> runtimeSettingsService,
            TelegramBotAdapter botAdapter
    ) {
        this.properties = properties;
        this.runtimeSettingsService = runtimeSettingsService;
        this.botAdapter = botAdapter;
    }

    TelegramWebhookController(AtlasProperties properties, TelegramBotAdapter botAdapter) {
        this.properties = properties;
        this.runtimeSettingsService = null;
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
        String expectedSecret = effectiveWebhookSecret();
        if (expectedSecret == null || expectedSecret.isBlank()) {
            return true;
        }

        if (secretToken == null || secretToken.isBlank()) {
            return false;
        }

        byte[] expected = expectedSecret.getBytes(StandardCharsets.UTF_8);
        byte[] actual = secretToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private String effectiveWebhookSecret() {
        AtlasRuntimeSettingsService service = runtimeSettingsService == null ? null : runtimeSettingsService.getIfAvailable();
        if (service != null) {
            EffectiveTelegramConfig config = service.effectiveTelegramConfig();
            if (config.hasWebhookSecret()) {
                return config.webhookSecret();
            }
        }
        return properties.telegram().webhookSecret();
    }
}
