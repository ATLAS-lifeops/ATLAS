package com.example.atlas.config;

import com.example.atlas.llm.LlmProvider;
import com.example.atlas.deployment.DeploymentMode;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "atlas")
public record AtlasProperties(Telegram telegram, Setup setup, Llm llm, Deployment deployment, Memory memory, Routines routines) {

    public AtlasProperties(Telegram telegram) {
        this(telegram, null, null, null, null, null);
    }

    public AtlasProperties(Telegram telegram, Setup setup, Llm llm) {
        this(telegram, setup, llm, null, null, null);
    }

    @ConstructorBinding
    public AtlasProperties {
        if (telegram == null) {
            telegram = new Telegram(false, "", "", TelegramLaunchMode.POLLING, "/telegram/webhook", "", "", "", false, true);
        }
        if (setup == null) {
            setup = new Setup(true);
        }
        if (llm == null) {
            llm = new Llm(
                    false,
                    LlmProvider.OPENAI_COMPATIBLE,
                    "",
                    "",
                    "",
                    20,
                    700,
                    0.3,
                    5,
                    true,
                    2,
                    true,
                    true,
                    true
            );
        }
        if (deployment == null) {
            deployment = new Deployment(DeploymentMode.SELF_HOSTED);
        }
        if (memory == null) {
            memory = new Memory(false, "/app/data/memory");
        }
        if (routines == null) {
            routines = new Routines(true);
        }
    }

    public record Telegram(
            boolean enabled,
            String botToken,
            String botUsername,
            TelegramLaunchMode mode,
            String webhookPath,
            String webhookUrl,
            String webhookSecret,
            String publicBaseUrl,
            boolean registerWebhookOnStartup,
            boolean dropPendingUpdatesOnWebhookRegistration
    ) {
        public Telegram {
            botToken = defaultString(botToken);
            botUsername = defaultString(botUsername);
            mode = mode == null ? TelegramLaunchMode.POLLING : mode;
            webhookPath = defaultString(webhookPath, "/telegram/webhook");
            webhookUrl = defaultString(webhookUrl);
            webhookSecret = defaultString(webhookSecret);
            publicBaseUrl = defaultString(publicBaseUrl);
        }

        public boolean hasBotToken() {
            return botToken != null && !botToken.isBlank();
        }

        public boolean hasWebhookSecret() {
            return webhookSecret != null && !webhookSecret.isBlank();
        }

        public boolean hasPublicBaseUrl() {
            return publicBaseUrl != null && !publicBaseUrl.isBlank();
        }

        public String effectiveWebhookUrl() {
            String strippedWebhookUrl = stripToNull(webhookUrl);
            return strippedWebhookUrl == null ? stripToNull(publicBaseUrl) : strippedWebhookUrl;
        }

        private static String defaultString(String value) {
            return defaultString(value, "");
        }

        private static String defaultString(String value, String fallback) {
            return value == null ? fallback : value;
        }

        private static String stripToNull(String value) {
            return value == null || value.isBlank() ? null : value.strip();
        }
    }

    public record Setup(boolean enabled) {
    }

    public record Llm(
            boolean enabled,
            LlmProvider provider,
            String baseUrl,
            String apiKey,
            String model,
            int timeoutSeconds,
            int maxOutputTokens,
            double temperature,
            int connectTimeoutSeconds,
            boolean retryEnabled,
            int maxRetries,
            boolean dayPlanEnabled,
            boolean reportEnabled,
            boolean questionEnabled
    ) {
        public Llm {
            provider = provider == null ? LlmProvider.OPENAI_COMPATIBLE : provider;
            baseUrl = defaultString(baseUrl);
            apiKey = defaultString(apiKey);
            model = defaultString(model);
            timeoutSeconds = timeoutSeconds <= 0 ? 20 : timeoutSeconds;
            maxOutputTokens = maxOutputTokens <= 0 ? 700 : maxOutputTokens;
            temperature = temperature < 0 ? 0.3 : temperature;
            connectTimeoutSeconds = connectTimeoutSeconds <= 0 ? 5 : connectTimeoutSeconds;
            maxRetries = Math.max(0, maxRetries);
        }

        public boolean configured() {
            return enabled
                    && provider != LlmProvider.DISABLED
                    && !baseUrl.isBlank()
                    && !apiKey.isBlank()
                    && !model.isBlank();
        }

        public boolean dayPlanAvailable() {
            return configured() && dayPlanEnabled;
        }

        public boolean reportAvailable() {
            return configured() && reportEnabled;
        }

        public boolean questionAvailable() {
            return configured() && questionEnabled;
        }

        public String safeBaseUrlHost() {
            if (baseUrl == null || baseUrl.isBlank()) {
                return "";
            }
            try {
                return java.net.URI.create(baseUrl).getHost();
            } catch (IllegalArgumentException exception) {
                return "";
            }
        }

        private static String defaultString(String value) {
            return value == null ? "" : value;
        }
    }

    public record Deployment(DeploymentMode mode) {
        public Deployment {
            mode = mode == null ? DeploymentMode.SELF_HOSTED : mode;
        }

        public boolean hosted() {
            return mode == DeploymentMode.HOSTED;
        }
    }

    public record Memory(boolean snapshotsEnabled, String snapshotPath) {
        public Memory {
            snapshotPath = snapshotPath == null || snapshotPath.isBlank() ? "/app/data/memory" : snapshotPath;
        }
    }

    public record Routines(boolean schedulerEnabled) {
    }
}
