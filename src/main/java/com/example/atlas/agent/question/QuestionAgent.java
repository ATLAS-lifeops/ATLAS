package com.example.atlas.agent.question;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.agent.AgentType;
import com.example.atlas.memory.MemoryConfidence;
import com.example.atlas.memory.MemoryScope;
import com.example.atlas.memory.MemorySource;
import com.example.atlas.memory.MemoryTag;
import com.example.atlas.memory.MemoryType;
import com.example.atlas.memory.MemoryWrite;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.llm.LlmQuestionAnswerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionAgent implements Agent {

    private final ObjectProvider<LlmQuestionAnswerService> questionAnswerService;

    @Autowired
    public QuestionAgent(ObjectProvider<LlmQuestionAnswerService> questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    public QuestionAgent() {
        this.questionAnswerService = null;
    }

    @Override
    public String name() {
        return "ATLAS Question";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.GENERAL;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String message = context.message() == null ? "" : context.message().toLowerCase();
        LlmQuestionAnswerService service = questionAnswerService == null ? null : questionAnswerService.getIfAvailable();
        if (service != null && context.user() != null) {
            return service.answer(context.user(), context.message())
                    .map(answer -> AgentResult.reply(answer, name()))
                    .orElseGet(() -> deterministicAnswer(context, message));
        }
        return deterministicAnswer(context, message);
    }

    private AgentResult deterministicAnswer(AgentContext context, String message) {
        if (outOfScope(message)) {
            return AgentResult.fallback(
                    "ATLAS отвечает только про планирование дня, привычки, check-ins, рефлексию, отчеты, состояние, фокус и ритм. Можно начать с /checkin, /day, /habits, /evening или /report.",
                    name()
            );
        }

        AgentResult result = AgentResult.reply(
                "В рамках ATLAS лучше сузить вопрос до одного шага: состояние сейчас, главный фокус, минимальная привычка или вечерний вывод. Начни с /checkin или попроси план через /day.",
                name()
        );
        if (context.internalUserId() == null || message.isBlank()) {
            return result;
        }
        MemoryWrite write = new MemoryWrite(
                context.internalUserId(),
                AgentType.QUESTION,
                MemoryType.PREFERENCE,
                MemoryScope.AGENT_PRIVATE,
                "Question topic",
                "User asks ATLAS-scoped questions about planning, habits or state.",
                MemoryConfidence.LOW,
                List.of(new MemoryTag("question")),
                MemorySource.QUESTION_AGENT,
                null
        );
        return result.withMemoryWrites(List.of(write));
    }

    private boolean outOfScope(String message) {
        return message.contains("stock")
                || message.contains("crypto")
                || message.contains("legal")
                || message.contains("diagnos")
                || message.contains("prescribe")
                || message.contains("курс валют")
                || message.contains("диагноз")
                || message.contains("юрид");
    }
}
