package com.example.atlas.architecture;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.planning.application.CreateDayPlanUseCase;
import com.example.atlas.profile.application.CompleteOnboardingStepUseCase;
import com.example.atlas.profile.application.RestartOnboardingUseCase;
import com.example.atlas.profile.application.StartOnboardingUseCase;
import com.example.atlas.reporting.application.BuildWeeklyReportUseCase;
import com.example.atlas.reporting.domain.WeeklyReport;
import com.example.atlas.shared.events.DomainEvent;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.tracking.application.CompleteCheckInUseCase;
import com.example.atlas.tracking.application.StartCheckInUseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModuleUseCaseSmokeTest {

    private final TelegramUserEntity user = TelegramUserEntity.create(
            7L,
            42L,
            "user",
            "User",
            Instant.parse("2026-05-27T10:00:00Z")
    );

    @Test
    void onboardingUseCasesRouteToConversationFlow() {
        TelegramLifeFlowService flowService = mock(TelegramLifeFlowService.class);
        TelegramLifeFlowService.FlowResult start = new TelegramLifeFlowService.FlowResult("onboarding started", RequestType.START);
        TelegramLifeFlowService.FlowResult step = new TelegramLifeFlowService.FlowResult("step completed", RequestType.START);
        TelegramLifeFlowService.FlowResult restart = new TelegramLifeFlowService.FlowResult("onboarding restarted", RequestType.START);
        when(flowService.handle(user, "/start", RequestType.START)).thenReturn(Optional.of(start));
        when(flowService.handle(user, "2", RequestType.GENERAL)).thenReturn(Optional.of(step));
        when(flowService.restartOnboarding(user)).thenReturn(restart);

        assertThat(new StartOnboardingUseCase(flowService).execute(new StartOnboardingUseCase.Input(user))).isSameAs(start);
        assertThat(new CompleteOnboardingStepUseCase(flowService).execute(new CompleteOnboardingStepUseCase.Input(user, "2"))).isSameAs(step);
        assertThat(new RestartOnboardingUseCase(flowService).execute(new RestartOnboardingUseCase.Input(user))).isSameAs(restart);
    }

    @Test
    void checkInUseCasesRouteToConversationFlow() {
        TelegramLifeFlowService flowService = mock(TelegramLifeFlowService.class);
        TelegramLifeFlowService.FlowResult start = new TelegramLifeFlowService.FlowResult("check-in started", RequestType.CHECKIN);
        TelegramLifeFlowService.FlowResult complete = new TelegramLifeFlowService.FlowResult("check-in saved", RequestType.CHECKIN);
        when(flowService.handle(user, "/checkin", RequestType.CHECKIN)).thenReturn(Optional.of(start));
        when(flowService.handle(user, "7", RequestType.GENERAL)).thenReturn(Optional.of(complete));

        assertThat(new StartCheckInUseCase(flowService).execute(new StartCheckInUseCase.Input(user))).isSameAs(start);
        assertThat(new CompleteCheckInUseCase(flowService).execute(new CompleteCheckInUseCase.Input(user, "7"))).isSameAs(complete);
    }

    @Test
    void planningAndReportingUseCasesReturnDomainResults() {
        LifeDayPlanService dayPlanService = mock(LifeDayPlanService.class);
        WeeklyLifeReportService reportService = mock(WeeklyLifeReportService.class);
        RecordingEventPublisher events = new RecordingEventPublisher();
        when(dayPlanService.dayPlan(user)).thenReturn("План дня");
        when(reportService.weeklyReport(user)).thenReturn("Недельный отчёт");

        assertThat(new CreateDayPlanUseCase(dayPlanService).execute(new CreateDayPlanUseCase.Input(user)).content())
                .contains("План дня");
        WeeklyReport report = new BuildWeeklyReportUseCase(reportService, events)
                .execute(new BuildWeeklyReportUseCase.Input(user));

        assertThat(report.content()).contains("Недельный отчёт");
        assertThat(events.events()).hasSize(1);
    }

    private static final class RecordingEventPublisher implements EventPublisher {

        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }

        List<DomainEvent> events() {
            return events;
        }
    }
}
