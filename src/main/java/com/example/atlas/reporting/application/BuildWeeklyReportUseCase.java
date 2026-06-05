package com.example.atlas.reporting.application;

import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.reporting.domain.WeeklyReport;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.WeeklyReportGeneratedEvent;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Supplier;

@Service
public class BuildWeeklyReportUseCase implements UseCase<BuildWeeklyReportUseCase.Input, WeeklyReport> {

    private final Supplier<WeeklyLifeReportService> reportService;
    private final EventPublisher eventPublisher;

    @Autowired
    public BuildWeeklyReportUseCase(ObjectProvider<WeeklyLifeReportService> reportService, EventPublisher eventPublisher) {
        this.reportService = reportService::getIfAvailable;
        this.eventPublisher = eventPublisher;
    }

    public BuildWeeklyReportUseCase(WeeklyLifeReportService reportService, EventPublisher eventPublisher) {
        this(() -> reportService, eventPublisher);
    }

    private BuildWeeklyReportUseCase(Supplier<WeeklyLifeReportService> reportService, EventPublisher eventPublisher) {
        this.reportService = reportService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public WeeklyReport execute(Input input) {
        WeeklyReport report = new WeeklyReport(requireReportService().weeklyReport(input.user()));
        eventPublisher.publish(new WeeklyReportGeneratedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                Instant.now()
        ));
        return report;
    }

    private WeeklyLifeReportService requireReportService() {
        WeeklyLifeReportService service = reportService.get();
        if (service == null) {
            throw new IllegalStateException("Weekly report service is not available.");
        }
        return service;
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
