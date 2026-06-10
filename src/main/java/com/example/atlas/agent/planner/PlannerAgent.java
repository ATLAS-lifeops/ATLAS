package com.example.atlas.agent.planner;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlannerAgent implements Agent {

    private final ObjectProvider<LifeDayPlanService> dayPlanService;

    @Autowired
    public PlannerAgent(ObjectProvider<LifeDayPlanService> dayPlanService) {
        this.dayPlanService = dayPlanService;
    }

    public PlannerAgent() {
        this.dayPlanService = null;
    }

    @Override
    public String name() {
        return "ATLAS Planner";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.DAY_PLAN || requestType == RequestType.WEEK_PLAN;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        LifeDayPlanService service = dayPlanService == null ? null : dayPlanService.getIfAvailable();
        if (service != null && context.user() != null && context.requestType() == RequestType.DAY_PLAN) {
            return AgentResult.reply(service.dayPlan(context.user()), name());
        }
        String content = switch (context.requestType()) {
            case DAY_PLAN -> "План дня: 1 главный фокус, короткий список действий, поддержка состояния, минимальная привычка и вечерняя рефлексия.";
            case WEEK_PLAN -> "План недели: несколько ключевых результатов, регулярные check-ins, привычки, восстановление ритма и короткий отчёт в конце недели.";
            default -> "ATLAS Planner подключён.";
        };

        return AgentResult.reply(content, name());
    }
}
