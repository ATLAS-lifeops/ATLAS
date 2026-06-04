package com.example.atlas.llm;

import com.example.atlas.config.AtlasProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LlmStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(LlmStartupLogger.class);

    private final AtlasProperties properties;

    public LlmStartupLogger(AtlasProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStatus() {
        AtlasProperties.Llm llm = properties.llm();
        log.info(
                "LLM configuration: enabled={}, configured={}, provider={}, model={}, base_url_host={}",
                llm.enabled(),
                llm.configured(),
                llm.provider(),
                safe(llm.model()),
                safe(llm.safeBaseUrlHost())
        );
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "not configured" : value;
    }
}
