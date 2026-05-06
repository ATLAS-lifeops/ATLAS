package com.example.atlas.agent.core;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
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
            case START -> "ATLAS на связи. Начнём спокойно: пришли /checkin, чтобы я понял состояние, или /day для плана на день.";
            case GENERAL -> "Я могу помочь с режимом, тренировкой, восстановлением, привычками и питанием. Быстрый старт: /checkin или /day.";
            default -> "Маршрут принят ATLAS Core.";
        };

        return AgentResult.reply(content, name());
    }
}
