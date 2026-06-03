package com.example.atlas.agent.coach;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

@Component
public class CoachAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Coach";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.WORKOUT || requestType == RequestType.CHECKIN;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case WORKOUT -> "Движение на сегодня: мягкая разминка, короткий рабочий блок по состоянию и спокойное завершение. Если есть боль или сильный перегруз, не усиливай нагрузку.";
            case CHECKIN -> "Для check-in оцени энергию, фокус, стресс, сон и настроение от 1 до 10.";
            default -> "ATLAS Coach подключён.";
        };

        return AgentResult.reply(content, name());
    }
}
