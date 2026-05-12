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
        return requestType == RequestType.START || requestType == RequestType.GENERAL;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case START -> TelegramReplyTemplates.startWelcome();
            case GENERAL -> TelegramReplyTemplates.generalFallback();
            default -> "Маршрут принят ATLAS Core.";
        };

        return AgentResult.reply(content, name());
    }
}
