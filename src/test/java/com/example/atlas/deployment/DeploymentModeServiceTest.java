package com.example.atlas.deployment;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentModeServiceTest {

    @Test
    void hostedModeRequiresWebhookSecret() {
        AtlasProperties withoutSecret = properties("");
        AtlasProperties withSecret = properties("secret");

        assertThat(new DeploymentModeService(withoutSecret).isSafe()).isFalse();
        assertThat(new DeploymentModeService(withSecret).isSafe()).isTrue();
    }

    private AtlasProperties properties(String webhookSecret) {
        return new AtlasProperties(
                new AtlasProperties.Telegram(
                        true,
                        "token",
                        "atlas_bot",
                        TelegramLaunchMode.WEBHOOK,
                        "/telegram/webhook",
                        "https://atlas.example/telegram/webhook",
                        webhookSecret,
                        "",
                        false,
                        true
                ),
                new AtlasProperties.Setup(false),
                null,
                new AtlasProperties.Deployment(DeploymentMode.HOSTED),
                null,
                null
        );
    }
}
