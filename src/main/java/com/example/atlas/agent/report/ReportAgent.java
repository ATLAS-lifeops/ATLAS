package com.example.atlas.agent.report;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.orchestrator.RequestType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportAgent implements Agent {

    private final ObjectProvider<ReportSummaryService> reportSummaryService;
    private final ObjectProvider<WeeklyLifeReportService> weeklyLifeReportService;

    @Autowired
    public ReportAgent(ObjectProvider<ReportSummaryService> reportSummaryService, ObjectProvider<WeeklyLifeReportService> weeklyLifeReportService) {
        this.reportSummaryService = reportSummaryService;
        this.weeklyLifeReportService = weeklyLifeReportService;
    }

    public ReportAgent() {
        this.reportSummaryService = null;
        this.weeklyLifeReportService = null;
    }

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
        WeeklyLifeReportService weeklyService = weeklyLifeReportService == null ? null : weeklyLifeReportService.getIfAvailable();
        if (weeklyService != null && context.user() != null) {
            return AgentResult.reply(weeklyService.weeklyReport(context.user()), name());
        }
        ReportSummaryService service = reportSummaryService == null ? null : reportSummaryService.getIfAvailable();
        if (service != null) {
            ReportSummaryService.ReportSummary summary = service.weeklySummary();
            if (summary.hasData()) {
                return AgentResult.reply(formatSummary(summary), name());
            }
        }

        return AgentResult.reply(
                "Недельный отчёт: что получилось, где сорвался ритм, какой один вывод берём в следующую неделю.",
                name()
        );
    }

    private String formatSummary(ReportSummaryService.ReportSummary summary) {
        String energy = summary.averageEnergy() == null ? "нет данных" : String.format("%.1f/10", summary.averageEnergy());
        String fatigue = summary.averageFatigue() == null ? "нет данных" : String.format("%.1f/10", summary.averageFatigue());
        String pain = summary.painMentioned() ? "были отметки боли, снизь нагрузку и проверь восстановление" : "отметок боли нет";

        return """
                Недельный отчёт:
                - check-ins: %d
                - входящих сообщений: %d
                - средняя энергия: %s
                - средняя усталость: %s
                - состояние: %s

                Вывод на неделю: держи регулярный check-in и корректируй план по энергии, усталости и боли.
                """.formatted(summary.checkIns(), summary.inboundMessages(), energy, fatigue, pain);
    }
}
