package com.example.atlas.telegram;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramActionRouterTest {

    private final TelegramActionRouter router = new TelegramActionRouter();

    @Test
    void commandAndCallbackMapToSameAction() {
        assertThat(router.actionForCommand("/checkin", false)).contains(TelegramAction.START_CHECKIN);
        assertThat(router.actionForCallback("atlas:checkin:start")).contains(TelegramAction.START_CHECKIN);
    }

    @Test
    void unsupportedCallbackIsRejected() {
        assertThat(router.isSupportedCallback("atlas:unknown")).isFalse();
        assertThat(router.actionForCallback("atlas:unknown")).isEmpty();
    }

    @Test
    void callbackFlowInputIsValidated() {
        assertThat(router.flowInputForCallback("atlas:checkin:value:energy:7")).contains("7");
        assertThat(router.flowInputForCallback("atlas:checkin:value:energy:999")).isEmpty();
        assertThat(router.flowInputForCallback("atlas:checkin:value:anything:7")).isEmpty();
        assertThat(router.flowInputForCallback("atlas:onboarding:life_area:FOCUS")).contains("FOCUS");
        assertThat(router.flowInputForCallback("atlas:onboarding:life_area:OTHER")).isEmpty();
    }

    @Test
    void languageCallbacksMapToActions() {
        assertThat(router.actionForCallback("atlas:language:ru")).contains(TelegramAction.SELECT_LANGUAGE_RU);
        assertThat(router.actionForCallback("atlas:language:en")).contains(TelegramAction.SELECT_LANGUAGE_EN);
        assertThat(router.actionForCallback("atlas:settings:language")).contains(TelegramAction.CHANGE_LANGUAGE);
    }
}
