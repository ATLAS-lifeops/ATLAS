package com.example.atlas.deployment;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import org.springframework.stereotype.Service;

@Service
public class DeploymentModeService {

    private final AtlasProperties properties;

    public DeploymentModeService(AtlasProperties properties) {
        this.properties = properties;
    }

    public DeploymentStatus status() {
        boolean webhookMode = properties.telegram().mode() == TelegramLaunchMode.WEBHOOK;
        return new DeploymentStatus(
                properties.deployment().mode(),
                properties.setup().enabled(),
                properties.telegram().enabled(),
                webhookMode,
                isSafe()
        );
    }

    public void validate() {
        if (!isSafe()) {
            throw new IllegalStateException("Unsafe ATLAS deployment configuration");
        }
    }

    public boolean isSafe() {
        if (!properties.deployment().hosted()) {
            return true;
        }
        return !properties.setup().enabled()
                && properties.telegram().enabled()
                && properties.telegram().hasBotToken()
                && properties.telegram().mode() == TelegramLaunchMode.WEBHOOK
                && properties.telegram().effectiveWebhookUrl() != null
                && properties.telegram().hasWebhookSecret();
    }
}
