package com.example.atlas.setup.controller;

import com.example.atlas.runtime.entity.TelegramLaunchMode;
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
        assertThat(html).contains("Setup saved");
    }

    private RuntimeSettingsStatus status(boolean setupCompleted) {
        return new RuntimeSettingsStatus(
                setupCompleted,
                TelegramLaunchMode.POLLING,
                "atlas_test_bot",
                true,
                false,
                "123...abc",
                null
        );
    }
}
