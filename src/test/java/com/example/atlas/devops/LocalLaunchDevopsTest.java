package com.example.atlas.devops;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalLaunchDevopsTest {

    @Test
    void startScriptSupportsFriendlyLocalLaunchFlow() throws Exception {
        Path script = Path.of("scripts/start.sh");
        String content = Files.readString(script);

        assertThat(Files.isExecutable(script)).isTrue();
        assertThat(content).contains("set -euo pipefail");
        assertThat(content).contains("docker compose up --build -d");
        assertThat(content).contains("ATLAS_APP_URL:-http://localhost:8080");
        assertThat(content).contains("/actuator/health");
        assertThat(content).contains("/setup");
        assertThat(content).contains("Open setup manually: $SETUP_URL");
        assertThat(content).doesNotContain("ATLAS_TELEGRAM_BOT_TOKEN=");
        assertThat(content).doesNotContain("Bot Token");
    }

    @Test
    void localEnvExampleUsesOnlySafePlaceholders() throws Exception {
        String content = Files.readString(Path.of(".env.example"));

        assertThat(content).contains("ATLAS_TELEGRAM_BOT_TOKEN=");
        assertThat(content).contains("ATLAS_TELEGRAM_MODE=polling");
        assertThat(content).contains("ATLAS_TELEGRAM_WEBHOOK_SECRET=");
        assertThat(content).doesNotContain("<token>");
        assertThat(content).doesNotContain("123456:");
    }
}
