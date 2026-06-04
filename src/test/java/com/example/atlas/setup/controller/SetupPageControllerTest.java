package com.example.atlas.setup.controller;

import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.service.LocalLaunchState;
import com.example.atlas.runtime.service.RuntimeSettingsStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SetupPageControllerTest {

    @Test
    void setupPageRendersCssPercentagesWithoutFormatterFailure() {
        String html = SetupPageController.setupPage(null, status(false));

        assertThat(html).contains("width: min(100%, 520px)");
        assertThat(html).contains("width: 100%");
        assertThat(html).contains("ATLAS");
    }

    @Test
    void successPageRendersCssPercentagesWithoutFormatterFailure() {
        String html = SetupPageController.successPage(status(true));

        assertThat(html).contains("width: min(100%, 520px)");
        assertThat(html).contains("ATLAS is running");
    }

    private RuntimeSettingsStatus status(boolean setupCompleted) {
        return new RuntimeSettingsStatus(
                !setupCompleted,
                setupCompleted,
                true,
                TelegramLaunchMode.POLLING,
                "atlas_test_bot",
                LocalLaunchState.TELEGRAM_POLLING_ACTIVE,
                "Active",
                true,
                false,
                null
        );
    }
}
