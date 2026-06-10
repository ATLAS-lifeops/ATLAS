package com.example.atlas.orchestrator;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.memory.AgentMemoryService;
import com.example.atlas.memory.MemoryWrite;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class OrchestratorService {

    private final List<Agent> agents;
    private final ObjectProvider<AgentMemoryService> memoryService;

    @Autowired
    public OrchestratorService(List<Agent> agents, ObjectProvider<AgentMemoryService> memoryService) {
        this.agents = agents.stream()
                .sorted(Comparator.comparing(Agent::name))
                .toList();
        this.memoryService = memoryService;
    }

    public OrchestratorService(List<Agent> agents) {
        this.agents = agents.stream()
                .sorted(Comparator.comparing(Agent::name))
                .toList();
        this.memoryService = null;
    }

    public AgentResult route(String message) {
        RequestType requestType = resolveRequestType(message);
        return route(requestType, message);
    }

    public AgentResult route(RequestType requestType, String message) {
        return route(null, requestType, message);
    }

    public AgentResult route(TelegramUserEntity user, RequestType requestType, String message) {
        AgentContext context = user == null
                ? AgentContext.anonymous(message, requestType)
                : AgentContext.forUser(user, message, requestType);

        List<AgentResult> results = agents.stream()
                .filter(agent -> agent.supports(requestType))
                .map(agent -> agent.handle(context))
                .toList();

        if (results.isEmpty()) {
            return AgentResult.reply(
                    "Я могу помочь собрать день в систему: /checkin, /day, /habits, /evening или /report.",
                    "ATLAS Core"
            );
        }

        if (results.size() == 1) {
            AgentResult result = results.getFirst();
            persistMemory(result.memoryWrites());
            return result;
        }

        String content = results.stream()
                .map(AgentResult::content)
                .reduce((left, right) -> left + "\n\n" + right)
                .orElseThrow();
        List<String> handledBy = results.stream()
                .flatMap(result -> result.handledBy().stream())
                .distinct()
                .toList();

        List<MemoryWrite> memoryWrites = results.stream()
                .flatMap(result -> result.memoryWrites().stream())
                .toList();
        persistMemory(memoryWrites);
        return new AgentResult(content, handledBy, java.util.Map.of(), false, false, memoryWrites);
    }

    private void persistMemory(List<MemoryWrite> memoryWrites) {
        if (memoryWrites == null || memoryWrites.isEmpty() || memoryService == null) {
            return;
        }
        AgentMemoryService service = memoryService.getIfAvailable();
        if (service == null) {
            return;
        }
        memoryWrites.forEach(service::write);
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
            case "/evening", "/review" -> RequestType.EVENING_REFLECTION;
            case "/food" -> RequestType.FOOD;
            case "/report" -> RequestType.REPORT;
            case "/privacy" -> RequestType.PRIVACY;
            case "/memory" -> RequestType.MEMORY;
            case "/export" -> RequestType.EXPORT;
            case "/forget" -> RequestType.FORGET;
            case "/delete_my_data" -> RequestType.DELETE_MY_DATA;
            case "/routines" -> RequestType.ROUTINES;
            case "/integrations" -> RequestType.INTEGRATIONS;
            case "/emergency" -> RequestType.EMERGENCY;
            case "/help" -> RequestType.HELP;
            case "/cancel" -> RequestType.CANCEL;
            case "/clear" -> RequestType.CLEAR;
            default -> RequestType.GENERAL;
        };
    }
}
