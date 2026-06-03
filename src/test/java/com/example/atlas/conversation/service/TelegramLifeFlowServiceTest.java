package com.example.atlas.conversation.service;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.checkin.service.CheckInPersistenceService;
import com.example.atlas.conversation.ConversationFlowType;
import com.example.atlas.conversation.entity.ConversationStateEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.LifeArea;
import com.example.atlas.life.PlanningStyle;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.life.service.WeeklyLifeReportService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramLifeFlowServiceTest {

    private final TelegramUserEntity user = TelegramUserEntity.create(7L, 42L, "user", "User", Instant.parse("2026-05-27T10:00:00Z"));
    private final LifeProfileEntity profile = LifeProfileEntity.create(user, Instant.parse("2026-05-27T10:00:00Z"));
    private final FakeConversationStateService conversationStateService = new FakeConversationStateService();
    private final FakeLifeProfileService lifeProfileService = new FakeLifeProfileService(profile);
    private final FakeCheckInPersistenceService checkInPersistenceService = new FakeCheckInPersistenceService();
    private final FakeHabitService habitService = new FakeHabitService();
    private final FakeEveningReflectionService reflectionService = new FakeEveningReflectionService();
    private final FakeDayPlanService dayPlanService = new FakeDayPlanService(lifeProfileService);
    private final FakeWeeklyReportService weeklyReportService = new FakeWeeklyReportService(lifeProfileService, habitService, reflectionService);
    private final TelegramLifeFlowService service = new TelegramLifeFlowService(
            conversationStateService,
            lifeProfileService,
            checkInPersistenceService,
            habitService,
            reflectionService,
            dayPlanService,
            weeklyReportService,
            new SafetyGuard()
    );

    @Test
    void startBeginsOnboardingForNewUser() {
        Optional<TelegramLifeFlowService.FlowResult> result = service.handle(user, "/start", RequestType.START);

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("Привет. Я ATLAS").contains("С чего начнём");
        assertThat(conversationStateService.active(user).orElseThrow().getFlowType()).isEqualTo(ConversationFlowType.ONBOARDING);
        assertThat(conversationStateService.active(user).orElseThrow().getStep()).isEqualTo("ASK_PRIMARY_LIFE_AREA");
    }

    @Test
    void startAfterCompletedOnboardingReturnsWelcomeBack() {
        profile.completeOnboarding(Instant.parse("2026-05-27T10:01:00Z"));

        Optional<TelegramLifeFlowService.FlowResult> result = service.handle(user, "/start", RequestType.START);

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("С возвращением").contains("/checkin");
    }

    @Test
    void onboardingAnswersAreSavedIntoProfileAndState() {
        conversationStateService.active = state(ConversationFlowType.ONBOARDING, "ASK_PRIMARY_LIFE_AREA");
        service.handle(user, "2", RequestType.GENERAL);
        service.handle(user, "Собрать рабочий день", RequestType.GENERAL);
        service.handle(user, "3", RequestType.GENERAL);
        service.handle(user, "1 3 6", RequestType.GENERAL);

        assertThat(profile.getPrimaryLifeArea()).isEqualTo(LifeArea.FOCUS);
        assertThat(profile.getCurrentFocus()).isEqualTo("Собрать рабочий день");
        assertThat(profile.getPlanningStyle()).isEqualTo(PlanningStyle.DETAILED);
        assertThat(profile.isSleepFocus()).isTrue();
        assertThat(profile.isHabitFocus()).isTrue();
        assertThat(profile.isOnboardingCompleted()).isTrue();
        assertThat(conversationStateService.completed).isNotNull();
    }

    @Test
    void cancelCancelsActiveFlow() {
        conversationStateService.active = state(ConversationFlowType.ONBOARDING, "ASK_CURRENT_FOCUS");

        Optional<TelegramLifeFlowService.FlowResult> result = service.handle(user, "/cancel", RequestType.CANCEL);

        assertThat(result).isPresent();
        assertThat(result.get().content()).contains("отменён");
        assertThat(conversationStateService.cancelled).isNotNull();
    }

    @Test
    void dailyCheckinValidatesNumericAnswersAndPersistsAtCompletion() {
        conversationStateService.active = state(ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY");

        Optional<TelegramLifeFlowService.FlowResult> invalid = service.handle(user, "11", RequestType.GENERAL);

        assertThat(invalid).isPresent();
        assertThat(invalid.get().content()).contains("Нужно число от 1 до 10");

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("energy", "7");
        payload.put("focus", "6");
        payload.put("stress", "5");
        payload.put("sleep", "6");
        payload.put("mood", "7");
        payload.put("main_priority", "Главная задача");
        conversationStateService.active = state(ConversationFlowType.DAILY_CHECKIN, "ASK_OVERLOAD_SIGNAL");
        conversationStateService.payloads.put(conversationStateService.active, payload);

        Optional<TelegramLifeFlowService.FlowResult> complete = service.handle(user, "нет", RequestType.GENERAL);

        assertThat(complete).isPresent();
        assertThat(complete.get().content()).contains("Check-in сохранён");
        assertThat(checkInPersistenceService.recordedEnergy).isEqualTo(7);
        assertThat(checkInPersistenceService.recordedFocus).isEqualTo(6);
        assertThat(checkInPersistenceService.recordedPriority).isEqualTo("Главная задача");
        assertThat(checkInPersistenceService.recordedOverload).isFalse();
        assertThat(conversationStateService.completed).isNotNull();
    }

    @Test
    void dayHabitsEveningAndReportCommandsRouteToLifeServices() {
        assertThat(service.handle(user, "/day", RequestType.DAY_PLAN).orElseThrow().content())
                .contains("План дня");
        assertThat(service.handle(user, "/habits", RequestType.HABITS).orElseThrow().content())
                .contains("Какую одну привычку");
        assertThat(service.handle(user, "/evening", RequestType.EVENING_REFLECTION).orElseThrow().content())
                .contains("Что сегодня получилось");
        assertThat(service.handle(user, "/report", RequestType.REPORT).orElseThrow().content())
                .contains("Недельный отчёт");
    }

    private ConversationStateEntity state(ConversationFlowType flowType, String step) {
        return ConversationStateEntity.active(user, flowType, step, "{}", Instant.parse("2026-05-27T10:00:00Z"));
    }

    private static class FakeConversationStateService extends ConversationStateService {

        private ConversationStateEntity active;
        private ConversationStateEntity completed;
        private ConversationStateEntity cancelled;
        private final Map<ConversationStateEntity, Map<String, String>> payloads = new IdentityHashMap<>();

        FakeConversationStateService() {
            super(null, new ObjectMapper());
        }

        @Override
        public ConversationStateEntity start(TelegramUserEntity user, ConversationFlowType flowType, String step) {
            active = ConversationStateEntity.active(user, flowType, step, "{}", Instant.parse("2026-05-27T10:00:00Z"));
            payloads.put(active, new LinkedHashMap<>());
            return active;
        }

        @Override
        public Optional<ConversationStateEntity> active(TelegramUserEntity user) {
            return Optional.ofNullable(active);
        }

        @Override
        public void moveTo(ConversationStateEntity state, String step, Map<String, String> payload) {
            state.moveTo(step, "{}", Instant.parse("2026-05-27T10:01:00Z"));
            payloads.put(state, new LinkedHashMap<>(payload));
        }

        @Override
        public void complete(ConversationStateEntity state, Map<String, String> payload) {
            completed = state;
            state.complete("{}", Instant.parse("2026-05-27T10:02:00Z"));
            payloads.put(state, new LinkedHashMap<>(payload));
            active = null;
        }

        @Override
        public void cancel(ConversationStateEntity state) {
            cancelled = state;
            state.cancel(Instant.parse("2026-05-27T10:02:00Z"));
            active = null;
        }

        @Override
        public Map<String, String> payload(ConversationStateEntity state) {
            return new LinkedHashMap<>(payloads.getOrDefault(state, new LinkedHashMap<>()));
        }
    }

    private static class FakeLifeProfileService extends LifeProfileService {

        private final LifeProfileEntity profile;

        FakeLifeProfileService(LifeProfileEntity profile) {
            super(null);
            this.profile = profile;
        }

        @Override
        public LifeProfileEntity getOrCreate(TelegramUserEntity user) {
            return profile;
        }
    }

    private static class FakeCheckInPersistenceService extends CheckInPersistenceService {

        private Integer recordedEnergy;
        private Integer recordedFocus;
        private String recordedPriority;
        private boolean recordedOverload;

        FakeCheckInPersistenceService() {
            super(null);
        }

        @Override
        public CheckInEntity recordFlow(
                TelegramUserEntity user,
                Integer energy,
                Integer focus,
                Integer stress,
                Integer sleepQuality,
                Integer mood,
                String mainPriority,
                boolean overloadFlag,
                boolean painFlag,
                String notes
        ) {
            recordedEnergy = energy;
            recordedFocus = focus;
            recordedPriority = mainPriority;
            recordedOverload = overloadFlag;
            return null;
        }
    }

    private static class FakeHabitService extends HabitService {
        FakeHabitService() {
            super(null);
        }
    }

    private static class FakeEveningReflectionService extends EveningReflectionService {
        FakeEveningReflectionService() {
            super(null);
        }
    }

    private static class FakeDayPlanService extends LifeDayPlanService {
        FakeDayPlanService(LifeProfileService lifeProfileService) {
            super(lifeProfileService, null);
        }

        @Override
        public String dayPlan(TelegramUserEntity user) {
            return "План дня\n\n1. Главный фокус";
        }
    }

    private static class FakeWeeklyReportService extends WeeklyLifeReportService {
        FakeWeeklyReportService(
                LifeProfileService lifeProfileService,
                HabitService habitService,
                EveningReflectionService reflectionService
        ) {
            super(lifeProfileService, null, habitService, reflectionService);
        }

        @Override
        public String weeklyReport(TelegramUserEntity user) {
            return "Недельный отчёт";
        }
    }
}
