package com.example.atlas.agent.recovery;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

@Component
public class RecoveryAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Recovery";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.RECOVERY
                || requestType == RequestType.CHECKIN
                || requestType == RequestType.EMERGENCY;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case RECOVERY -> "Оценка восстановления: сон, уровень стресса, мышечная усталость и признаки болезни важнее плана нагрузки.";
            case CHECKIN -> "По восстановлению отметь сон, стресс и общее состояние. Если есть сильная боль, проблемы с дыханием, сердцем или давлением, лучше обратиться к специалисту.";
            case EMERGENCY -> "Режим восстановления: вода, еда без крайностей, 10 минут прогулки или растяжки, ранний сон.";
            default -> "ATLAS Recovery подключён.";
        };

        return AgentResult.reply(content, name());
    }
}
