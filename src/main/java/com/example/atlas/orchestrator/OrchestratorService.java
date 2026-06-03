package com.example.atlas.orchestrator;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class OrchestratorService {

    private final List<Agent> agents;

    public OrchestratorService(List<Agent> agents) {
        this.agents = agents.stream()
                .sorted(Comparator.comparing(Agent::name))
                .toList();
    }

    public AgentResult route(String message) {
        RequestType requestType = resolveRequestType(message);
        return route(requestType, message);
    }

    public AgentResult route(RequestType requestType, String message) {
        AgentContext context = AgentContext.anonymous(message, requestType);

        List<AgentResult> results = agents.stream()
                .filter(agent -> agent.supports(requestType))
                .map(agent -> agent.handle(context))
                .toList();

        if (results.isEmpty()) {
            return AgentResult.reply(
                    "Я пока не понял запрос. Можно начать с /day, /workout, /checkin или /emergency.",
                    "ATLAS Core"
            );
        }

        if (results.size() == 1) {
            return results.getFirst();
        }

        String content = results.stream()
                .map(AgentResult::content)
                .reduce((left, right) -> left + "\n\n" + right)
                .orElseThrow();
        List<String> handledBy = results.stream()
                .flatMap(result -> result.handledBy().stream())
                .distinct()
                .toList();

        return new AgentResult(content, handledBy);
    }

    public RequestType resolveRequestType(String message) {
        if (message == null || message.isBlank()) {
            return RequestType.GENERAL;
        }

        String command = message.trim().toLowerCase(Locale.ROOT).split("\\s+", 2)[0];

        return switch (command) {
            case "/start" -> RequestType.START;
            case "/day" -> RequestType.DAY_PLAN;
            case "/week" -> RequestType.WEEK_PLAN;
            case "/workout" -> RequestType.WORKOUT;
            case "/checkin" -> RequestType.CHECKIN;
            case "/recovery" -> RequestType.RECOVERY;
            case "/habits" -> RequestType.HABITS;
            case "/food" -> RequestType.FOOD;
            case "/report" -> RequestType.REPORT;
            case "/emergency" -> RequestType.EMERGENCY;
            default -> RequestType.GENERAL;
        };
    }
}
