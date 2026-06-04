package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

public class LlmClientFactory {

    private final AtlasProperties.Llm properties;
    private final ObjectMapper objectMapper;

    public LlmClientFactory(AtlasProperties.Llm properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public LlmClient create() {
        if (properties == null || !properties.configured() || properties.provider() != LlmProvider.OPENAI_COMPATIBLE) {
            return new DisabledLlmClient();
        }
        return new OpenAICompatibleLlmClient(
                properties.baseUrl(),
                properties.apiKey(),
                Duration.ofSeconds(properties.connectTimeoutSeconds()),
                objectMapper,
                properties.retryEnabled(),
                properties.maxRetries()
        );
    }
}
