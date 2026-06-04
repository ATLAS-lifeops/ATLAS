package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmClientFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void disabledByDefaultCreatesDisabledClient() {
        LlmClient client = new LlmClientFactory(properties(false, "", "", ""), objectMapper).create();

        assertThat(client).isInstanceOf(DisabledLlmClient.class);
        assertThat(client.available()).isFalse();
    }

    @Test
    void incompleteEnabledConfigCreatesDisabledClient() {
        LlmClient client = new LlmClientFactory(properties(true, "http://localhost:1", "", "model"), objectMapper).create();

        assertThat(client).isInstanceOf(DisabledLlmClient.class);
    }

    @Test
    void completeOpenAiCompatibleConfigCreatesProviderClient() {
        LlmClient client = new LlmClientFactory(properties(true, "http://localhost:1", "test-key", "model"), objectMapper).create();

        assertThat(client).isInstanceOf(OpenAICompatibleLlmClient.class);
        assertThat(client.provider()).isEqualTo(LlmProvider.OPENAI_COMPATIBLE);
    }

    private AtlasProperties.Llm properties(boolean enabled, String baseUrl, String apiKey, String model) {
        return new AtlasProperties.Llm(
                enabled,
                LlmProvider.OPENAI_COMPATIBLE,
                baseUrl,
                apiKey,
                model,
                20,
                700,
                0.3,
                1,
                false,
                0,
                true,
                true,
                true
        );
    }
}
