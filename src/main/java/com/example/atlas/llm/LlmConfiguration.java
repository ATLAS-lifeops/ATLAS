package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfiguration {

    @Bean
    public LlmClient llmClient(AtlasProperties properties, ObjectMapper objectMapper) {
        return new LlmClientFactory(properties.llm(), objectMapper).create();
    }
}
