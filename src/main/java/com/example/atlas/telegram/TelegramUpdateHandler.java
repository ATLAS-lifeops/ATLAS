package com.example.atlas.telegram;

import com.example.atlas.orchestrator.OrchestratorService;
import org.springframework.stereotype.Component;

@Component
public class TelegramUpdateHandler {

    private final OrchestratorService orchestratorService;

    public TelegramUpdateHandler(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    public String handleTextMessage(String text) {
        return orchestratorService.route(text).content();
    }
}
