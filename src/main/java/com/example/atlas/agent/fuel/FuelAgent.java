package com.example.atlas.agent.fuel;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

@Component
public class FuelAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Fuel";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.FOOD;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        return AgentResult.reply(
                "Питание на день: регулярные приёмы пищи, белок в каждый основной приём, вода и без экстремальных ограничений.",
                name()
        );
    }
}
