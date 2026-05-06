package com.example.atlas.agent.report;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

@Component
public class ReportAgent implements Agent {

    @Override
    public String name() {
        return "ATLAS Report";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.REPORT;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        return AgentResult.reply(
                "Недельный отчёт: что получилось, где сорвался ритм, какой один вывод берём в следующую неделю.",
                name()
        );
    }
}
