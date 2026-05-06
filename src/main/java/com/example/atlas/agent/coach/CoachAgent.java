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
            case WORKOUT -> "Тренировка на сегодня: 10 минут разминки, 3 рабочих блока по самочувствию, затем спокойная заминка. Если есть боль, нагрузку останавливаем.";
            case CHECKIN -> "По тренировкам мне нужны три числа: энергия 1-10, усталость 1-10, есть ли боль или травма.";
            default -> "ATLAS Coach подключён.";
        };

        return AgentResult.reply(content, name());
    }
}
