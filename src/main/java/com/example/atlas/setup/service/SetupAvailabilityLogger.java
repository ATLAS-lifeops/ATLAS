package com.example.atlas.setup.service;

import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(AtlasRuntimeSettingsService.class)
public class SetupAvailabilityLogger {

    private static final Logger log = LoggerFactory.getLogger(SetupAvailabilityLogger.class);

    private final AtlasRuntimeSettingsService runtimeSettingsService;

    public SetupAvailabilityLogger(AtlasRuntimeSettingsService runtimeSettingsService) {
        this.runtimeSettingsService = runtimeSettingsService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logSetupUrl() {
        if (!runtimeSettingsService.isSetupCompleted()) {
            log.info("ATLAS setup is available at http://localhost:8080/setup");
        }
    }
}
