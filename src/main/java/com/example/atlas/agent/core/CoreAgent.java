package com.example.atlas.agent.core;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.telegram.TelegramReplyTemplates;
import org.springframework.stereotype.Component;

@Component
public class CoreAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Core";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.START
                || requestType == RequestType.GENERAL
                || requestType == RequestType.PRIVACY
                || requestType == RequestType.MEMORY
                || requestType == RequestType.EXPORT
                || requestType == RequestType.FORGET
                || requestType == RequestType.DELETE_MY_DATA
                || requestType == RequestType.ROUTINES
                || requestType == RequestType.INTEGRATIONS;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case START -> TelegramReplyTemplates.startWelcome();
            case GENERAL -> TelegramReplyTemplates.generalFallback();
            case PRIVACY -> "Privacy: ATLAS stores profile, check-ins, habits, reflections, reports, memory and Telegram identifiers. Use /export, /forget or /delete_my_data for data controls.";
            case MEMORY -> "Memory: ATLAS can store user-scoped preferences, patterns and summaries. Raw sensitive content is not shown by default.";
            case EXPORT -> "Export: ATLAS prepares user-scoped JSON and Markdown data export when persistence is available.";
            case FORGET -> "Forget memory: this action clears memory only after explicit confirmation.";
            case DELETE_MY_DATA -> "Delete my data: this destructive action requires explicit confirmation and applies only to the current user.";
            case ROUTINES -> "Routines: check-in and evening reminder preferences include timezone, quiet hours and enabled state.";
            case INTEGRATIONS -> "Integrations: Markdown export and calendar contracts are available as foundations without external sync.";
            default -> "Маршрут принят ATLAS Core.";
        };

        return AgentResult.reply(content, name());
    }
}
