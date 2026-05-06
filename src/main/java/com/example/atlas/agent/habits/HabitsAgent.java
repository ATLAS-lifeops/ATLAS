package com.example.atlas.agent.habits;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

@Component
public class HabitsAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Habits";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.HABITS || requestType == RequestType.EMERGENCY;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case HABITS -> "Привычки: выбери одну базовую привычку на сегодня и минимальную версию, которую точно можно выполнить.";
            case EMERGENCY -> "Минимальный план: одно полезное действие на 5 минут, одно бытовое действие и короткий чек-ин вечером.";
            default -> "ATLAS Habits подключён.";
        };

        return AgentResult.reply(content, name());
    }
}
