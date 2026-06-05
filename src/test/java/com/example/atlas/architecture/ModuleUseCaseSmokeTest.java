package com.example.atlas.architecture;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.conversation.service.ConversationStateService;
import com.example.atlas.checkin.service.CheckInPersistenceService;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.planning.application.CreateDayPlanUseCase;
import com.example.atlas.profile.application.CompleteOnboardingStepUseCase;
import com.example.atlas.profile.application.RestartOnboardingUseCase;
import com.example.atlas.profile.application.StartOnboardingUseCase;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.reporting.application.BuildWeeklyReportUseCase;
import com.example.atlas.reporting.domain.WeeklyReport;
import com.example.atlas.safety.SafetyGuard;
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
        TelegramLifeFlowService.FlowResult start = new TelegramLifeFlowService.FlowResult("onboarding started", RequestType.START);
        TelegramLifeFlowService.FlowResult step = new TelegramLifeFlowService.FlowResult("step completed", RequestType.START);
        TelegramLifeFlowService.FlowResult restart = new TelegramLifeFlowService.FlowResult("onboarding restarted", RequestType.START);
        FakeFlowService flowService = new FakeFlowService(start, step, restart);

        assertThat(new StartOnboardingUseCase(flowService).execute(new StartOnboardingUseCase.Input(user))).isSameAs(start);
        assertThat(new CompleteOnboardingStepUseCase(flowService).execute(new CompleteOnboardingStepUseCase.Input(user, "2"))).isSameAs(step);
        assertThat(new RestartOnboardingUseCase(flowService).execute(new RestartOnboardingUseCase.Input(user))).isSameAs(restart);
    }

    @Test
    void checkInUseCasesRouteToConversationFlow() {
        TelegramLifeFlowService.FlowResult start = new TelegramLifeFlowService.FlowResult("check-in started", RequestType.CHECKIN);
        TelegramLifeFlowService.FlowResult complete = new TelegramLifeFlowService.FlowResult("check-in saved", RequestType.CHECKIN);
        FakeFlowService flowService = new FakeFlowService(start, complete, null);

        assertThat(new StartCheckInUseCase(flowService).execute(new StartCheckInUseCase.Input(user))).isSameAs(start);
        assertThat(new CompleteCheckInUseCase(flowService).execute(new CompleteCheckInUseCase.Input(user, "7"))).isSameAs(complete);
    }

    @Test
    void planningAndReportingUseCasesReturnDomainResults() {
        LifeDayPlanService dayPlanService = new FakeDayPlanService("План дня");
        WeeklyLifeReportService reportService = new FakeWeeklyReportService("Недельный отчёт");
        RecordingEventPublisher events = new RecordingEventPublisher();

        assertThat(new CreateDayPlanUseCase(dayPlanService).execute(new CreateDayPlanUseCase.Input(user)).content())
                .contains("План дня");
        WeeklyReport report = new BuildWeeklyReportUseCase(reportService, events)
                .execute(new BuildWeeklyReportUseCase.Input(user));

        assertThat(report.content()).contains("Недельный отчёт");
        assertThat(events.events()).hasSize(1);
    }

    private static final class FakeFlowService extends TelegramLifeFlowService {

        private final TelegramLifeFlowService.FlowResult commandResult;
        private final TelegramLifeFlowService.FlowResult generalResult;
        private final TelegramLifeFlowService.FlowResult restartResult;

        private FakeFlowService(
                TelegramLifeFlowService.FlowResult commandResult,
                TelegramLifeFlowService.FlowResult generalResult,
                TelegramLifeFlowService.FlowResult restartResult
        ) {
            super(
                    (ConversationStateService) null,
                    (LifeProfileService) null,
                    (CheckInPersistenceService) null,
                    (HabitService) null,
                    (EveningReflectionService) null,
                    (LifeDayPlanService) null,
                    (WeeklyLifeReportService) null,
                    new SafetyGuard()
            );
            this.commandResult = commandResult;
            this.generalResult = generalResult;
            this.restartResult = restartResult;
        }

        @Override
        public Optional<TelegramLifeFlowService.FlowResult> handle(
                TelegramUserEntity user,
                String text,
                RequestType requestType
        ) {
            return requestType == RequestType.GENERAL ? Optional.of(generalResult) : Optional.of(commandResult);
        }

        @Override
        public TelegramLifeFlowService.FlowResult restartOnboarding(TelegramUserEntity user) {
            return restartResult;
        }
    }

    private static final class FakeDayPlanService extends LifeDayPlanService {

        private final String content;

        private FakeDayPlanService(String content) {
            super((LifeProfileService) null, (CheckInRepository) null);
            this.content = content;
        }

        @Override
        public String dayPlan(TelegramUserEntity user) {
            return content;
        }
    }

    private static final class FakeWeeklyReportService extends WeeklyLifeReportService {

        private final String content;

        private FakeWeeklyReportService(String content) {
            super(
                    (LifeProfileService) null,
                    (CheckInRepository) null,
                    (HabitService) null,
                    (EveningReflectionService) null
            );
            this.content = content;
        }

        @Override
        public String weeklyReport(TelegramUserEntity user) {
            return content;
        }
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
