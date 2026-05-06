package com.example.atlas.agent;

import com.example.atlas.orchestrator.RequestType;

public interface Agent {

    String name();

    boolean supports(RequestType requestType);

    AgentResult handle(AgentContext context);
}
