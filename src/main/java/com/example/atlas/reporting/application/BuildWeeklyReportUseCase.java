package com.example.atlas.reporting.application;

import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.reporting.domain.WeeklyReport;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.WeeklyReportGeneratedEvent;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@ConditionalOnBean(WeeklyLifeReportService.class)
public class BuildWeeklyReportUseCase implements UseCase<BuildWeeklyReportUseCase.Input, WeeklyReport> {

    private final WeeklyLifeReportService reportService;
    private final EventPublisher eventPublisher;

    public BuildWeeklyReportUseCase(WeeklyLifeReportService reportService, EventPublisher eventPublisher) {
        this.reportService = reportService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WeeklyReport execute(Input input) {
        WeeklyReport report = new WeeklyReport(reportService.weeklyReport(input.user()));
        eventPublisher.publish(new WeeklyReportGeneratedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                Instant.now()
        ));
        return report;
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
