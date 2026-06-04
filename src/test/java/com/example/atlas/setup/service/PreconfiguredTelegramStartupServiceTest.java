package com.example.atlas.setup.service;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.entity.AtlasRuntimeSettingsEntity;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.repository.AtlasRuntimeSettingsRepository;
import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.LocalLaunchState;
import com.example.atlas.runtime.service.RuntimeSettingsStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PreconfiguredTelegramStartupServiceTest {

    @Test
    void missingTelegramTokenLeavesSetupRequiredState() {
        AtlasRuntimeSettingsService settingsService = settingsService(properties(""));

        RuntimeSettingsStatus status = settingsService.status();

        assertThat(status.setupRequired()).isTrue();
        assertThat(status.telegramConfigured()).isFalse();
        assertThat(status.state()).isEqualTo(LocalLaunchState.SETUP_REQUIRED);
    }

    @Test
    void validEnvironmentTokenSavesPollingRuntimeSettings() {
        AtlasRuntimeSettingsService settingsService = settingsService(properties("test-token"));
        PreconfiguredTelegramStartupService startupService = new PreconfiguredTelegramStartupService(
                properties("test-token"),
                settingsService,
                validator(new TelegramBotIdentity(42L, "atlas_test_bot", "ATLAS"))
        );

        startupService.run(null);

        RuntimeSettingsStatus status = settingsService.status();
        assertThat(status.setupCompleted()).isTrue();
        assertThat(status.telegramConfigured()).isTrue();
        assertThat(status.telegramMode()).isEqualTo(TelegramLaunchMode.POLLING);
        assertThat(status.botUsername()).isEqualTo("atlas_test_bot");
        assertThat(status.state()).isEqualTo(LocalLaunchState.TELEGRAM_POLLING_ACTIVE);
    }

    @Test
    void invalidEnvironmentTokenCreatesSafeSetupError() {
        AtlasRuntimeSettingsService settingsService = settingsService(properties("test-token"));
        PreconfiguredTelegramStartupService startupService = new PreconfiguredTelegramStartupService(
                properties("test-token"),
                settingsService,
                failingValidator()
        );

        startupService.run(null);

        RuntimeSettingsStatus status = settingsService.status();
        assertThat(status.state()).isEqualTo(LocalLaunchState.SETUP_ERROR);
        assertThat(status.telegramConfigured()).isFalse();
        assertThat(status.setupError()).contains("Replace it through setup");
        assertThat(status.setupError()).doesNotContain("test-token");
    }

    @Test
    void statusJsonDoesNotExposeSecrets() throws Exception {
        AtlasRuntimeSettingsService settingsService = settingsService(properties("test-token"));
        settingsService.saveTelegramSetup(
                "test-token",
                "atlas_test_bot",
                TelegramLaunchMode.WEBHOOK,
                "https://atlas.example/telegram/webhook",
                "webhook-secret"
        );

        String json = new ObjectMapper().writeValueAsString(settingsService.status());

        assertThat(json).doesNotContain("test-token");
        assertThat(json).doesNotContain("webhook-secret");
        assertThat(json).contains("\"telegramConfigured\":true");
    }

    private AtlasRuntimeSettingsService settingsService(AtlasProperties properties) {
        AtomicReference<AtlasRuntimeSettingsEntity> saved = new AtomicReference<>();
        AtlasRuntimeSettingsRepository repository = repository(saved);
        return new AtlasRuntimeSettingsService(properties, provider(repository));
    }

    private AtlasRuntimeSettingsRepository repository(AtomicReference<AtlasRuntimeSettingsEntity> saved) {
        return (AtlasRuntimeSettingsRepository) Proxy.newProxyInstance(
                AtlasRuntimeSettingsRepository.class.getClassLoader(),
                new Class<?>[]{AtlasRuntimeSettingsRepository.class},
                (proxy, method, args) -> {
                    if ("findFirstByOrderByCreatedAtAsc".equals(method.getName())) {
                        return Optional.ofNullable(saved.get());
                    }
                    if ("save".equals(method.getName())) {
                        AtlasRuntimeSettingsEntity entity = (AtlasRuntimeSettingsEntity) args[0];
                        saved.set(entity);
                        return entity;
                    }
                    if ("toString".equals(method.getName())) {
                        return "InMemoryAtlasRuntimeSettingsRepository";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private AtlasProperties properties(String botToken) {
        return new AtlasProperties(new AtlasProperties.Telegram(
                botToken != null && !botToken.isBlank(),
                botToken,
                "",
                TelegramLaunchMode.POLLING,
                "/telegram/webhook",
                "",
                "",
                "",
                false,
                true
        ));
    }

    private TelegramBotTokenValidator validator(TelegramBotIdentity identity) {
        return new TelegramBotTokenValidator(RestClient.builder()) {
            @Override
            public TelegramBotIdentity validate(String botToken) {
                return identity;
            }
        };
    }

    private TelegramBotTokenValidator failingValidator() {
        return new TelegramBotTokenValidator(RestClient.builder()) {
            @Override
            public TelegramBotIdentity validate(String botToken) {
                throw new TelegramBotTokenValidationException("Telegram token is invalid.");
            }
        };
    }

    private ObjectProvider<AtlasRuntimeSettingsRepository> provider(AtlasRuntimeSettingsRepository repository) {
        return new ObjectProvider<>() {
            @Override
            public AtlasRuntimeSettingsRepository getObject(Object... args) {
                return repository;
            }

            @Override
            public AtlasRuntimeSettingsRepository getIfAvailable() {
                return repository;
            }

            @Override
            public AtlasRuntimeSettingsRepository getIfUnique() {
                return repository;
            }

            @Override
            public AtlasRuntimeSettingsRepository getObject() {
                return repository;
            }
        };
    }
}
