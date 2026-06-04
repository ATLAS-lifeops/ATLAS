package com.example.atlas.conversation.service;

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
import com.example.atlas.llm.LlmQuestionAnswerService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.telegram.InlineKeyboardMarkup;
import com.example.atlas.telegram.TelegramKeyboardFactory;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@ConditionalOnBean({
        ConversationStateService.class,
        LifeProfileService.class,
        CheckInPersistenceService.class,
        HabitService.class,
        EveningReflectionService.class,
        LifeDayPlanService.class,
        WeeklyLifeReportService.class
})
public class TelegramLifeFlowService {

    private final ConversationStateService conversationStateService;
    private final LifeProfileService lifeProfileService;
    private final CheckInPersistenceService checkInPersistenceService;
    private final HabitService habitService;
    private final EveningReflectionService reflectionService;
    private final LifeDayPlanService dayPlanService;
    private final WeeklyLifeReportService weeklyReportService;
    private final SafetyGuard safetyGuard;
    private final TelegramKeyboardFactory keyboardFactory;
    private final ObjectProvider<LlmQuestionAnswerService> llmQuestionAnswerService;

    @Autowired
    public TelegramLifeFlowService(
            ConversationStateService conversationStateService,
            LifeProfileService lifeProfileService,
            CheckInPersistenceService checkInPersistenceService,
            HabitService habitService,
            EveningReflectionService reflectionService,
            LifeDayPlanService dayPlanService,
            WeeklyLifeReportService weeklyReportService,
            SafetyGuard safetyGuard,
            TelegramKeyboardFactory keyboardFactory,
            ObjectProvider<LlmQuestionAnswerService> llmQuestionAnswerService
    ) {
        this.conversationStateService = conversationStateService;
        this.lifeProfileService = lifeProfileService;
        this.checkInPersistenceService = checkInPersistenceService;
        this.habitService = habitService;
        this.reflectionService = reflectionService;
        this.dayPlanService = dayPlanService;
        this.weeklyReportService = weeklyReportService;
        this.safetyGuard = safetyGuard;
        this.keyboardFactory = keyboardFactory;
        this.llmQuestionAnswerService = llmQuestionAnswerService;
    }

    public TelegramLifeFlowService(
            ConversationStateService conversationStateService,
            LifeProfileService lifeProfileService,
            CheckInPersistenceService checkInPersistenceService,
            HabitService habitService,
            EveningReflectionService reflectionService,
            LifeDayPlanService dayPlanService,
            WeeklyLifeReportService weeklyReportService,
            SafetyGuard safetyGuard,
            TelegramKeyboardFactory keyboardFactory
    ) {
        this(
                conversationStateService,
                lifeProfileService,
                checkInPersistenceService,
                habitService,
                reflectionService,
                dayPlanService,
                weeklyReportService,
                safetyGuard,
                keyboardFactory,
                null
        );
    }

    public TelegramLifeFlowService(
            ConversationStateService conversationStateService,
            LifeProfileService lifeProfileService,
            CheckInPersistenceService checkInPersistenceService,
            HabitService habitService,
            EveningReflectionService reflectionService,
            LifeDayPlanService dayPlanService,
            WeeklyLifeReportService weeklyReportService,
            SafetyGuard safetyGuard
    ) {
        this(
                conversationStateService,
                lifeProfileService,
                checkInPersistenceService,
                habitService,
                reflectionService,
                dayPlanService,
                weeklyReportService,
                safetyGuard,
                new TelegramKeyboardFactory(),
                null
        );
    }

    @Transactional
    public Optional<FlowResult> handle(TelegramUserEntity user, String text, RequestType requestType) {
        if (user == null) {
            return Optional.empty();
        }

        if (requestType == RequestType.CANCEL) {
            return Optional.of(cancel(user));
        }
        if (requestType == RequestType.HELP) {
            return Optional.of(new FlowResult(help(), requestType, keyboardFactory.help()));
        }
        if (requestType == RequestType.START) {
            return Optional.of(startOnboardingOrWelcomeBack(user));
        }
        if (requestType == RequestType.CHECKIN) {
            return Optional.of(handleCheckinCommand(user, text));
        }
        if (requestType == RequestType.DAY_PLAN) {
            if (!hasCheckIns(user)) {
                UserLanguage language = language(user);
                return Optional.of(new FlowResult(dayPlanEmptyState(language), requestType, keyboardFactory.dayPlanEmptyStateActions(language)));
            }
            return Optional.of(new FlowResult(dayPlanService.dayPlan(user), requestType, keyboardFactory.dayPlanActions(language(user))));
        }
        if (requestType == RequestType.HABITS) {
            UserLanguage language = language(user);
            return Optional.of(startFlow(user, ConversationFlowType.HABIT_TRACKING, "ASK_HABIT", habitEmptyState(language)));
        }
        if (requestType == RequestType.EVENING_REFLECTION) {
            UserLanguage language = language(user);
            return Optional.of(startFlow(user, ConversationFlowType.EVENING_REFLECTION, "ASK_MAIN_RESULT", prompt(ConversationFlowType.EVENING_REFLECTION, "ASK_MAIN_RESULT", language)));
        }
        if (requestType == RequestType.REPORT) {
            UserLanguage language = language(user);
            if (!hasUsefulReportData(user)) {
                return Optional.of(new FlowResult(reportEmptyState(language), requestType, keyboardFactory.reportActions(language)));
            }
            return Optional.of(new FlowResult(weeklyReportService.weeklyReport(user), requestType, keyboardFactory.reportActions(language)));
        }
        if (requestType == RequestType.EMERGENCY) {
            return Optional.of(new FlowResult(emergency(text, language(user)), requestType, keyboardFactory.backToMenu(language(user))));
        }

        if (!isCommand(text)) {
            Optional<ConversationStateEntity> active = conversationStateService.active(user);
            if (active.isPresent()) {
                return Optional.of(continueFlow(active.get(), text));
            }
            if (requestType == RequestType.GENERAL) {
                return answerQuestion(user, text);
            }
        }

        return Optional.empty();
    }

    private Optional<FlowResult> answerQuestion(TelegramUserEntity user, String text) {
        LlmQuestionAnswerService service = llmQuestionAnswerService == null ? null : llmQuestionAnswerService.getIfAvailable();
        if (service == null) {
            return Optional.empty();
        }
        UserLanguage language = language(user);
        return service.answer(user, text)
                .map(answer -> new FlowResult(answer, RequestType.GENERAL, keyboardFactory.questionActions(language)));
    }

    private FlowResult startOnboardingOrWelcomeBack(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        if (active.isPresent()) {
            return activeFlowContinuation(user);
        }
        LifeProfileEntity profile = lifeProfileService.getOrCreate(user);
        if (profile.isOnboardingCompleted()) {
            UserLanguage language = language(user);
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "ATLAS\n\nWhat would you like to do now?"
                            : "ATLAS\n\nЧто хочешь сделать сейчас?",
                    RequestType.START,
                    keyboardFactory.mainMenu(language)
            );
        }
        conversationStateService.start(user, ConversationFlowType.ONBOARDING, "ASK_PRIMARY_LIFE_AREA");
        UserLanguage language = language(user);
        return new FlowResult(onboardingIntro(language), RequestType.START, keyboardFactory.onboardingLifeAreas(language));
    }

    private FlowResult handleCheckinCommand(TelegramUserEntity user, String text) {
        if (hasStructuredCheckinValues(text)) {
            checkInPersistenceService.record(user, text);
            String safety = safetyGuard.requiresSafetyResponse(text) ? "\n\n" + safetyGuard.safetyResponse() : "";
            UserLanguage language = language(user);
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "Check-in saved. Use /day to build a realistic day plan." + safety
                            : "Check-in сохранён. Используй /day, чтобы собрать реалистичный план дня." + safety,
                    RequestType.CHECKIN,
                    keyboardFactory.checkinCompleteActions(language)
            );
        }
        UserLanguage language = language(user);
        return startFlow(user, ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY", prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY", language));
    }

    private FlowResult startFlow(TelegramUserEntity user, ConversationFlowType flowType, String step, String prompt) {
        ConversationStateEntity state = conversationStateService.start(user, flowType, step);
        return new FlowResult(prompt, requestType(flowType), keyboardFactory.forActiveStep(state, language(user)));
    }

    @Transactional(readOnly = true)
    public boolean hasActiveFlow(TelegramUserEntity user) {
        return user != null && conversationStateService.active(user).isPresent();
    }

    @Transactional(readOnly = true)
    public FlowResult activeFlowContinuation(TelegramUserEntity user) {
        UserLanguage language = language(user);
        if (user == null || conversationStateService.active(user).isEmpty()) {
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "This flow is no longer available. Returning to menu."
                            : "Сценарий устарел. Вернёмся в меню.",
                    RequestType.GENERAL,
                    keyboardFactory.mainMenu(language)
            );
        }
        return new FlowResult(
                language == UserLanguage.EN
                        ? "You have an unfinished flow.\n\nWhat would you like to do?"
                        : "У тебя есть незавершённый сценарий.\n\nЧто сделать?",
                RequestType.GENERAL,
                keyboardFactory.activeFlowContinuation(language)
        );
    }

    @Transactional
    public FlowResult continueActiveFlow(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        UserLanguage language = language(user);
        if (active.isEmpty()) {
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "This flow is no longer available. Returning to menu."
                            : "Сценарий устарел. Вернёмся в меню.",
                    RequestType.GENERAL,
                    keyboardFactory.mainMenu(language)
            );
        }
        ConversationStateEntity state = active.get();
        return new FlowResult(
                prompt(state.getFlowType(), state.getStep(), language),
                requestType(state.getFlowType()),
                keyboardFactory.forActiveStep(state, language)
        );
    }

    @Transactional
    public FlowResult back(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        UserLanguage language = language(user);
        if (active.isEmpty()) {
            return continueActiveFlow(user);
        }
        ConversationStateEntity state = active.get();
        StepBack previous = previousStep(state);
        if (previous == null) {
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "Back is not available here. You can continue or cancel."
                            : "Назад здесь недоступно. Можно продолжить или отменить.",
                    requestType(state.getFlowType()),
                    keyboardFactory.forActiveStep(state, language)
            );
        }
        Map<String, String> payload = conversationStateService.payload(state);
        Arrays.stream(previous.keysToRemove()).forEach(payload::remove);
        conversationStateService.moveTo(state, previous.step(), payload);
        return new FlowResult(
                prompt(state.getFlowType(), previous.step(), language),
                requestType(state.getFlowType()),
                keyboardFactory.forActiveStep(state, language)
        );
    }

    @Transactional(readOnly = true)
    public FlowResult restartActiveFlowConfirmation(TelegramUserEntity user) {
        UserLanguage language = language(user);
        return new FlowResult(
                language == UserLanguage.EN
                        ? "Restart this flow?\n\nCurrent answers in the unfinished flow will be replaced."
                        : "Начать этот сценарий заново?\n\nТекущие ответы в незавершённом сценарии будут заменены.",
                RequestType.GENERAL,
                keyboardFactory.restartActiveFlowConfirmation(language)
        );
    }

    @Transactional
    public FlowResult restartActiveFlow(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        UserLanguage language = language(user);
        if (active.isEmpty()) {
            return activeFlowContinuation(user);
        }
        ConversationFlowType flowType = active.get().getFlowType();
        String firstStep = firstStep(flowType);
        ConversationStateEntity state = conversationStateService.start(user, flowType, firstStep);
        return new FlowResult(prompt(flowType, firstStep, language), requestType(flowType), keyboardFactory.forActiveStep(state, language));
    }

    private FlowResult continueFlow(ConversationStateEntity state, String text) {
        return switch (state.getFlowType()) {
            case ONBOARDING -> continueOnboarding(state, text);
            case DAILY_CHECKIN -> continueDailyCheckin(state, text);
            case HABIT_TRACKING -> continueHabitTracking(state, text);
            case EVENING_REFLECTION -> continueEveningReflection(state, text);
            case DAY_PLAN -> new FlowResult(dayPlanService.dayPlan(state.getTelegramUser()), RequestType.DAY_PLAN);
        };
    }

    private FlowResult continueOnboarding(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        LifeProfileEntity profile = lifeProfileService.getOrCreate(state.getTelegramUser());
        UserLanguage language = language(state.getTelegramUser());
        Instant now = Instant.now();
        switch (state.getStep()) {
            case "ASK_PRIMARY_LIFE_AREA" -> {
                LifeArea area = parseLifeArea(text);
                profile.updatePrimaryLifeArea(area, now);
                payload.put("primary_life_area", area.name());
                conversationStateService.moveTo(state, "ASK_CURRENT_FOCUS", payload);
                return new FlowResult(prompt(ConversationFlowType.ONBOARDING, "ASK_CURRENT_FOCUS", language), RequestType.START, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_CURRENT_FOCUS" -> {
                payload.put("current_focus", clean(text));
                profile.updateCurrentFocus(clean(text), now);
                conversationStateService.moveTo(state, "ASK_PLANNING_STYLE", payload);
                return new FlowResult(prompt(ConversationFlowType.ONBOARDING, "ASK_PLANNING_STYLE", language), RequestType.START, keyboardFactory.planningStyles(language));
            }
            case "ASK_PLANNING_STYLE" -> {
                PlanningStyle style = parsePlanningStyle(text);
                payload.put("planning_style", style.name());
                profile.updatePlanningStyle(style, now);
                conversationStateService.moveTo(state, "ASK_MAIN_LOOPS", payload);
                return new FlowResult(prompt(ConversationFlowType.ONBOARDING, "ASK_MAIN_LOOPS", language), RequestType.START, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_MAIN_LOOPS" -> {
                String loops = text == null ? "" : text;
                profile.updateLifeLoops(
                        containsChoice(loops, "1", "сон", "sleep"),
                        containsChoice(loops, "2", "стресс", "stress", "energy", "энерг"),
                        containsChoice(loops, "3", "привыч", "habit"),
                        containsChoice(loops, "4", "питан", "nutrition", "food"),
                        containsChoice(loops, "5", "движ", "movement"),
                        containsChoice(loops, "6", "фокус", "focus", "tasks", "задач"),
                        now
                );
                payload.put("life_loops", clean(text));
                profile.completeOnboarding(now);
                conversationStateService.complete(state, payload);
                return new FlowResult(
                        language == UserLanguage.EN
                                ? "Done. I saved your profile. Start with a short check-in."
                                : "Готово. Я сохранил профиль. Начнём с короткого check-in.",
                        RequestType.START,
                        keyboardFactory.onboardingCompleteActions(language)
                );
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_PRIMARY_LIFE_AREA", payload);
                return new FlowResult(onboardingIntro(language), RequestType.START, keyboardFactory.onboardingLifeAreas(language));
            }
        }
    }

    private FlowResult continueDailyCheckin(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        UserLanguage language = language(state.getTelegramUser());
        switch (state.getStep()) {
            case "ASK_ENERGY" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult(invalidScore(language) + "\n\n" + prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY", language), RequestType.CHECKIN, keyboardFactory.score("energy", language, false));
                }
                payload.put("energy", value.toString());
                conversationStateService.moveTo(state, "ASK_FOCUS", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_FOCUS", language), RequestType.CHECKIN, keyboardFactory.score("focus", language, true));
            }
            case "ASK_FOCUS" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult(invalidScore(language) + "\n\n" + prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_FOCUS", language), RequestType.CHECKIN, keyboardFactory.score("focus", language, true));
                }
                payload.put("focus", value.toString());
                conversationStateService.moveTo(state, "ASK_STRESS", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_STRESS", language), RequestType.CHECKIN, keyboardFactory.score("stress", language, true));
            }
            case "ASK_STRESS" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult(invalidScore(language) + "\n\n" + prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_STRESS", language), RequestType.CHECKIN, keyboardFactory.score("stress", language, true));
                }
                payload.put("stress", value.toString());
                conversationStateService.moveTo(state, "ASK_SLEEP", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_SLEEP", language), RequestType.CHECKIN, keyboardFactory.score("sleep", language, true));
            }
            case "ASK_SLEEP" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult(invalidScore(language) + "\n\n" + prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_SLEEP", language), RequestType.CHECKIN, keyboardFactory.score("sleep", language, true));
                }
                payload.put("sleep", value.toString());
                conversationStateService.moveTo(state, "ASK_MOOD", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_MOOD", language), RequestType.CHECKIN, keyboardFactory.score("mood", language, true));
            }
            case "ASK_MOOD" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult(invalidScore(language) + "\n\n" + prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_MOOD", language), RequestType.CHECKIN, keyboardFactory.score("mood", language, true));
                }
                payload.put("mood", value.toString());
                conversationStateService.moveTo(state, "ASK_MAIN_PRIORITY", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_MAIN_PRIORITY", language), RequestType.CHECKIN, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_MAIN_PRIORITY" -> {
                payload.put("main_priority", clean(text));
                conversationStateService.moveTo(state, "ASK_OVERLOAD_SIGNAL", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_OVERLOAD_SIGNAL", language), RequestType.CHECKIN, keyboardFactory.yesNo("atlas:checkin:overload:yes", "atlas:checkin:overload:no", language, true));
            }
            case "ASK_OVERLOAD_SIGNAL" -> {
                payload.put("overload_signal", clean(text));
                boolean risk = safetyGuard.requiresSafetyResponse(text) || yes(text) && hasRiskWords(text);
                checkInPersistenceService.recordFlow(
                        state.getTelegramUser(),
                        integer(payload.get("energy")),
                        integer(payload.get("focus")),
                        integer(payload.get("stress")),
                        integer(payload.get("sleep")),
                        integer(payload.get("mood")),
                        payload.get("main_priority"),
                        risk,
                        safetyGuard.requiresSafetyResponse(text),
                        payload.get("overload_signal")
                );
                conversationStateService.complete(state, payload);
                String safety = risk ? "\n\n" + safetyGuard.safetyResponse() : "";
                String content = language == UserLanguage.EN ? """
                        Check-in saved.
                        Energy: %s/10, focus: %s/10, stress: %s/10, sleep: %s/10, mood: %s/10.
                        Main priority today: %s.
                        """ : """
                        Check-in сохранён.
                        Энергия: %s/10, фокус: %s/10, стресс: %s/10, сон: %s/10, настроение: %s/10.
                        Главное сегодня: %s.
                        """;
                return new FlowResult(content.formatted(
                        payload.get("energy"),
                        payload.get("focus"),
                        payload.get("stress"),
                        payload.get("sleep"),
                        payload.get("mood"),
                        payload.get("main_priority")
                ).strip() + safety, RequestType.CHECKIN, keyboardFactory.checkinCompleteActions(language));
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_ENERGY", payload);
                return new FlowResult(prompt(ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY", language), RequestType.CHECKIN, keyboardFactory.score("energy", language, false));
            }
        }
    }

    private FlowResult continueHabitTracking(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        UserLanguage language = language(state.getTelegramUser());
        switch (state.getStep()) {
            case "ASK_HABIT" -> {
                payload.put("habit", clean(text));
                conversationStateService.moveTo(state, "ASK_MINIMUM_VERSION", payload);
                return new FlowResult(prompt(ConversationFlowType.HABIT_TRACKING, "ASK_MINIMUM_VERSION", language), RequestType.HABITS, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_MINIMUM_VERSION" -> {
                payload.put("minimum_version", clean(text));
                conversationStateService.moveTo(state, "ASK_COMPLETION", payload);
                return new FlowResult(prompt(ConversationFlowType.HABIT_TRACKING, "ASK_COMPLETION", language), RequestType.HABITS, keyboardFactory.yesNo("atlas:habit:completed:yes", "atlas:habit:completed:no", language, true));
            }
            case "ASK_COMPLETION" -> {
                boolean completed = yes(text);
                payload.put("completed", Boolean.toString(completed));
                habitService.record(
                        state.getTelegramUser(),
                        payload.get("habit"),
                        payload.get("minimum_version"),
                        completed,
                        clean(text)
                );
                conversationStateService.complete(state, payload);
                return new FlowResult(
                        language == UserLanguage.EN
                                ? "Habit saved. Minimum version: " + payload.get("minimum_version") + "."
                                : "Привычка сохранена. Минимальная версия: " + payload.get("minimum_version") + ".",
                        RequestType.HABITS,
                        keyboardFactory.habitCompleteActions(language)
                );
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_HABIT", payload);
                return new FlowResult(habitEmptyState(language), RequestType.HABITS, keyboardFactory.forActiveStep(state, language));
            }
        }
    }

    private FlowResult continueEveningReflection(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        UserLanguage language = language(state.getTelegramUser());
        switch (state.getStep()) {
            case "ASK_MAIN_RESULT" -> {
                payload.put("main_result", clean(text));
                conversationStateService.moveTo(state, "ASK_MAIN_BLOCKER", payload);
                return new FlowResult(prompt(ConversationFlowType.EVENING_REFLECTION, "ASK_MAIN_BLOCKER", language), RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_MAIN_BLOCKER" -> {
                payload.put("main_blocker", clean(text));
                conversationStateService.moveTo(state, "ASK_TOMORROW_FOCUS", payload);
                return new FlowResult(prompt(ConversationFlowType.EVENING_REFLECTION, "ASK_TOMORROW_FOCUS", language), RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state, language));
            }
            case "ASK_TOMORROW_FOCUS" -> {
                payload.put("tomorrow_focus", clean(text));
                reflectionService.record(
                        state.getTelegramUser(),
                        payload.get("main_result"),
                        payload.get("main_blocker"),
                        payload.get("tomorrow_focus")
                );
                conversationStateService.complete(state, payload);
                return new FlowResult(
                        language == UserLanguage.EN
                                ? "Reflection saved. Tomorrow's focus: " + payload.get("tomorrow_focus") + "."
                                : "Рефлексия сохранена. Завтра держим фокус: " + payload.get("tomorrow_focus") + ".",
                        RequestType.EVENING_REFLECTION,
                        keyboardFactory.eveningCompleteActions(language)
                );
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_MAIN_RESULT", payload);
                return new FlowResult(prompt(ConversationFlowType.EVENING_REFLECTION, "ASK_MAIN_RESULT", language), RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state, language));
            }
        }
    }

    private FlowResult cancel(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        active.ifPresent(conversationStateService::cancel);
        UserLanguage language = language(user);
        String text = active.isPresent()
                ? (language == UserLanguage.EN
                ? "Current flow cancelled. You can start again from the menu."
                : "Текущий сценарий отменён. Можно начать заново из меню.")
                : (language == UserLanguage.EN
                ? "There is no active flow. You can start from the menu."
                : "Активного сценария нет. Можно начать из меню.");
        return new FlowResult(text, RequestType.CANCEL, keyboardFactory.backToMenu(language));
    }

    @Transactional(readOnly = true)
    public String settings(TelegramUserEntity user) {
        UserLanguage language = language(user);
        Optional<LifeProfileEntity> profile = lifeProfileService.find(user);
        if (profile.isEmpty()) {
            if (language == UserLanguage.EN) {
                return """
                        ⚙️ Settings

                        Language: English
                        Onboarding: not completed
                        Primary loop: not selected
                        Planning style: not selected
                        Important loops: not selected

                        Telegram secrets are not shown here.
                        """;
            }
            return """
                    ⚙️ Настройки

                    Язык: Русский
                    Onboarding: не завершён
                    Основной контур: не выбран
                    Стиль плана: не выбран
                    Важные контуры: не выбраны

                    Секреты Telegram здесь не показываются.
                    """;
        }
        LifeProfileEntity value = profile.get();
        if (language == UserLanguage.EN) {
            return """
                    ⚙️ Settings

                    Language: English
                    Onboarding: %s
                    Primary loop: %s
                    Planning style: %s
                    Important loops: %s

                    Telegram secrets are not shown here.
                    """.formatted(
                    value.isOnboardingCompleted() ? "completed" : "not completed",
                    areaText(value.getPrimaryLifeArea(), language),
                    planningStyleText(value.getPlanningStyle(), language),
                    lifeLoops(value, language)
            ).strip();
        }
        return """
                ⚙️ Настройки

                Язык: Русский
                Onboarding: %s
                Основной контур: %s
                Стиль плана: %s
                Важные контуры: %s

                Секреты Telegram здесь не показываются.
                """.formatted(
                value.isOnboardingCompleted() ? "завершён" : "не завершён",
                areaText(value.getPrimaryLifeArea(), language),
                planningStyleText(value.getPlanningStyle(), language),
                lifeLoops(value, language)
        ).strip();
    }

    @Transactional(readOnly = true)
    public FlowResult profile(TelegramUserEntity user) {
        UserLanguage language = language(user);
        Optional<LifeProfileEntity> profile = lifeProfileService.find(user);
        if (profile.isEmpty() || !profile.get().isOnboardingCompleted()) {
            return new FlowResult(
                    language == UserLanguage.EN
                            ? "🧭 ATLAS profile\n\nProfile is incomplete. Finish onboarding to make ATLAS more useful."
                            : "🧭 Профиль ATLAS\n\nПрофиль не завершён. Заверши onboarding, чтобы ATLAS был полезнее.",
                    RequestType.GENERAL,
                    keyboardFactory.onboardingCompleteActions(language)
            );
        }
        LifeProfileEntity value = profile.get();
        String content = language == UserLanguage.EN ? """
                🧭 ATLAS profile

                Primary loop: %s
                Current focus: %s
                Planning style: %s
                Important loops: %s
                """ : """
                🧭 Профиль ATLAS

                Главный контур: %s
                Текущий фокус: %s
                Стиль плана: %s
                Важные контуры: %s
                """;
        return new FlowResult(
                content.formatted(
                        areaText(value.getPrimaryLifeArea(), language),
                        value.getCurrentFocus() == null || value.getCurrentFocus().isBlank()
                                ? (language == UserLanguage.EN ? "not selected" : "не выбран")
                                : value.getCurrentFocus(),
                        planningStyleText(value.getPlanningStyle(), language),
                        lifeLoops(value, language)
                ).strip(),
                RequestType.GENERAL,
                keyboardFactory.settingsActions(language)
        );
    }

    @Transactional
    public FlowResult restartOnboarding(TelegramUserEntity user) {
        conversationStateService.active(user).ifPresent(conversationStateService::cancel);
        lifeProfileService.getOrCreate(user);
        conversationStateService.start(user, ConversationFlowType.ONBOARDING, "ASK_PRIMARY_LIFE_AREA");
        UserLanguage language = language(user);
        return new FlowResult(onboardingIntro(language), RequestType.START, keyboardFactory.onboardingLifeAreas(language));
    }

    private String onboardingIntro(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return """
                    Hi. I am ATLAS. I help you notice state, keep focus, track habits and return the day to a manageable rhythm.

                    Where should we start?
                    1 - Daily rhythm
                    2 - Focus and tasks
                    3 - Habits
                    4 - Energy and recovery
                    5 - Movement
                    6 - Nutrition
                    7 - General balance
                    """;
        }
        return """
                Привет. Я ATLAS. Я помогаю видеть состояние, держать фокус, отслеживать привычки и возвращать день в управляемый ритм.

                С чего начнём?
                1 - Режим дня
                2 - Фокус и задачи
                3 - Привычки
                4 - Энергия и восстановление
                5 - Движение
                6 - Питание
                7 - Общая собранность
                """;
    }

    private String help() {
        return """
                Команды ATLAS

                /start - onboarding / restart introduction
                /checkin - daily state check-in
                /day - realistic day plan
                /habits - habit tracking
                /evening или /review - evening reflection
                /report - weekly report
                /cancel - cancel current flow
                /emergency - minimal plan when the day falls apart
                """;
    }

    private String emergency(String text, UserLanguage language) {
        String safety = safetyGuard.requiresSafetyResponse(text) ? "\n\n" + safetyGuard.safetyResponse() : "";
        if (language == UserLanguage.EN) {
            return """
                    Minimal plan

                    1. Pause for 2 minutes.
                    2. Choose one required task.
                    3. Remove everything optional.
                    4. Take one small next step.
                    5. Return to a short evening reflection later.
                    """.strip() + safety;
        }
        return """
                Минимальный план

                1. Остановиться на 2 минуты.
                2. Выбрать одну обязательную задачу.
                3. Убрать всё необязательное.
                4. Сделать один маленький шаг.
                5. Вечером вернуться к короткой рефлексии.
                """.strip() + safety;
    }

    private boolean hasStructuredCheckinValues(String text) {
        CheckInPersistenceService.ParsedCheckIn parsed = checkInPersistenceService.parse(text);
        return parsed.energy() != null
                || parsed.focus() != null
                || parsed.stress() != null
                || parsed.sleepQuality() != null
                || parsed.mood() != null
                || parsed.fatigue() != null;
    }

    private LifeArea parseLifeArea(String text) {
        String normalized = normalize(text);
        if (matches(normalized, "1", "режим", "daily")) {
            return LifeArea.DAILY_STRUCTURE;
        }
        if (matches(normalized, "2", "фокус", "задач", "focus")) {
            return LifeArea.FOCUS;
        }
        if (matches(normalized, "3", "привыч", "habit")) {
            return LifeArea.HABITS;
        }
        if (matches(normalized, "4", "энерг", "восстанов", "energy", "recovery")) {
            return LifeArea.ENERGY;
        }
        if (matches(normalized, "5", "движ", "movement")) {
            return LifeArea.MOVEMENT;
        }
        if (matches(normalized, "6", "питан", "nutrition", "food")) {
            return LifeArea.NUTRITION;
        }
        return LifeArea.GENERAL_BALANCE;
    }

    private PlanningStyle parsePlanningStyle(String text) {
        String normalized = normalize(text);
        if (matches(normalized, "1", "миним", "minimal")) {
            return PlanningStyle.MINIMAL;
        }
        if (matches(normalized, "3", "подроб", "detailed")) {
            return PlanningStyle.DETAILED;
        }
        return PlanningStyle.BALANCED;
    }

    private Integer parseScore(String text) {
        try {
            int value = Integer.parseInt(clean(text));
            return value >= 1 && value <= 10 ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer integer(String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean containsChoice(String value, String... choices) {
        String normalized = normalize(value);
        return Arrays.stream(choices).anyMatch(choice -> normalized.contains(choice.toLowerCase(Locale.ROOT)));
    }

    private boolean matches(String normalized, String... choices) {
        return Arrays.stream(choices).anyMatch(choice -> normalized.equals(choice) || normalized.contains(choice.toLowerCase(Locale.ROOT)));
    }

    private boolean yes(String text) {
        String normalized = normalize(text);
        return normalized.startsWith("да")
                || normalized.startsWith("yes")
                || normalized.equals("y")
                || normalized.equals("+")
                || normalized.equals("true");
    }

    private boolean hasRiskWords(String text) {
        String normalized = normalize(text);
        return normalized.contains("боль")
                || normalized.contains("перегруз")
                || normalized.contains("тревож")
                || normalized.contains("pain")
                || normalized.contains("overload")
                || normalized.contains("worry");
    }

    private RequestType requestType(ConversationFlowType flowType) {
        return switch (flowType) {
            case ONBOARDING -> RequestType.START;
            case DAILY_CHECKIN -> RequestType.CHECKIN;
            case HABIT_TRACKING -> RequestType.HABITS;
            case EVENING_REFLECTION -> RequestType.EVENING_REFLECTION;
            case DAY_PLAN -> RequestType.DAY_PLAN;
        };
    }

    private StepBack previousStep(ConversationStateEntity state) {
        if (state == null) {
            return null;
        }
        if (state.getFlowType() == ConversationFlowType.ONBOARDING) {
            return switch (state.getStep()) {
                case "ASK_CURRENT_FOCUS" -> new StepBack("ASK_PRIMARY_LIFE_AREA", "primary_life_area");
                case "ASK_PLANNING_STYLE" -> new StepBack("ASK_CURRENT_FOCUS", "current_focus");
                case "ASK_MAIN_LOOPS" -> new StepBack("ASK_PLANNING_STYLE", "planning_style");
                default -> null;
            };
        }
        if (state.getFlowType() == ConversationFlowType.DAILY_CHECKIN) {
            return switch (state.getStep()) {
                case "ASK_FOCUS" -> new StepBack("ASK_ENERGY", "energy");
                case "ASK_STRESS" -> new StepBack("ASK_FOCUS", "focus");
                case "ASK_SLEEP" -> new StepBack("ASK_STRESS", "stress");
                case "ASK_MOOD" -> new StepBack("ASK_SLEEP", "sleep");
                case "ASK_MAIN_PRIORITY" -> new StepBack("ASK_MOOD", "mood");
                case "ASK_OVERLOAD_SIGNAL" -> new StepBack("ASK_MAIN_PRIORITY", "main_priority");
                default -> null;
            };
        }
        if (state.getFlowType() == ConversationFlowType.HABIT_TRACKING) {
            return switch (state.getStep()) {
                case "ASK_MINIMUM_VERSION" -> new StepBack("ASK_HABIT", "habit");
                case "ASK_COMPLETION" -> new StepBack("ASK_MINIMUM_VERSION", "minimum_version");
                default -> null;
            };
        }
        if (state.getFlowType() == ConversationFlowType.EVENING_REFLECTION) {
            return switch (state.getStep()) {
                case "ASK_MAIN_BLOCKER" -> new StepBack("ASK_MAIN_RESULT", "main_result");
                case "ASK_TOMORROW_FOCUS" -> new StepBack("ASK_MAIN_BLOCKER", "main_blocker");
                default -> null;
            };
        }
        return null;
    }

    private String firstStep(ConversationFlowType flowType) {
        return switch (flowType) {
            case ONBOARDING -> "ASK_PRIMARY_LIFE_AREA";
            case DAILY_CHECKIN -> "ASK_ENERGY";
            case HABIT_TRACKING -> "ASK_HABIT";
            case EVENING_REFLECTION -> "ASK_MAIN_RESULT";
            case DAY_PLAN -> "ASK_DAY";
        };
    }

    private String prompt(ConversationFlowType flowType, String step, UserLanguage language) {
        if (flowType == ConversationFlowType.ONBOARDING) {
            return onboardingPrompt(step, language);
        }
        if (flowType == ConversationFlowType.DAILY_CHECKIN) {
            return checkinPrompt(step, language);
        }
        if (flowType == ConversationFlowType.HABIT_TRACKING) {
            return habitPrompt(step, language);
        }
        if (flowType == ConversationFlowType.EVENING_REFLECTION) {
            return eveningPrompt(step, language);
        }
        return language == UserLanguage.EN ? "Continue the current flow." : "Продолжим текущий сценарий.";
    }

    private String onboardingPrompt(String step, UserLanguage language) {
        return switch (step) {
            case "ASK_PRIMARY_LIFE_AREA" -> onboardingIntro(language);
            case "ASK_CURRENT_FOCUS" -> language == UserLanguage.EN
                    ? "What is the most important thing to bring into order now? Reply with one short phrase."
                    : "Что сейчас важнее всего привести в порядок? Ответь коротко одной фразой.";
            case "ASK_PLANNING_STYLE" -> language == UserLanguage.EN ? """
                    Which planning style fits you best?
                    1 - Minimal
                    2 - Balanced
                    3 - Detailed
                    """ : """
                    Какой стиль плана тебе ближе?
                    1 - Минимальный
                    2 - Сбалансированный
                    3 - Подробный
                    """;
            case "ASK_MAIN_LOOPS" -> language == UserLanguage.EN ? """
                    Which life loops matter most right now? You can choose several numbers:
                    1 - Sleep
                    2 - Energy / stress
                    3 - Habits
                    4 - Nutrition
                    5 - Movement
                    6 - Focus and tasks
                    """ : """
                    Какие контуры жизни сейчас особенно важны? Можно выбрать несколько цифр:
                    1 - Сон
                    2 - Энергия / стресс
                    3 - Привычки
                    4 - Питание
                    5 - Движение
                    6 - Фокус и задачи
                    """;
            default -> onboardingIntro(language);
        };
    }

    private String checkinPrompt(String step, UserLanguage language) {
        return switch (step) {
            case "ASK_ENERGY" -> language == UserLanguage.EN ? "Rate your energy from 1 to 10." : "Оцени энергию от 1 до 10.";
            case "ASK_FOCUS" -> language == UserLanguage.EN ? "Rate your focus from 1 to 10." : "Оцени фокус от 1 до 10.";
            case "ASK_STRESS" -> language == UserLanguage.EN ? "Rate your stress from 1 to 10." : "Оцени стресс от 1 до 10.";
            case "ASK_SLEEP" -> language == UserLanguage.EN ? "Rate your sleep from 1 to 10." : "Оцени сон от 1 до 10.";
            case "ASK_MOOD" -> language == UserLanguage.EN ? "Rate your mood from 1 to 10." : "Оцени настроение от 1 до 10.";
            case "ASK_MAIN_PRIORITY" -> language == UserLanguage.EN ? "What is the main thing today?" : "Что сегодня главное?";
            case "ASK_OVERLOAD_SIGNAL" -> language == UserLanguage.EN
                    ? "Any pain, strong overload or worrying symptom? Reply yes/no and add a few words if needed."
                    : "Есть ли боль, сильный перегруз или тревожный симптом? Ответь да/нет и добавь пару слов, если нужно.";
            default -> language == UserLanguage.EN ? "Continue the check-in." : "Продолжим check-in.";
        };
    }

    private String habitPrompt(String step, UserLanguage language) {
        return switch (step) {
            case "ASK_HABIT" -> habitEmptyState(language);
            case "ASK_MINIMUM_VERSION" -> language == UserLanguage.EN
                    ? "What is the 2-5 minute minimum version of this habit?"
                    : "Какая минимальная версия этой привычки займёт 2-5 минут?";
            case "ASK_COMPLETION" -> language == UserLanguage.EN
                    ? "Have you completed it today? Yes/no."
                    : "Сегодня она уже выполнена? Да/нет.";
            default -> habitEmptyState(language);
        };
    }

    private String eveningPrompt(String step, UserLanguage language) {
        return switch (step) {
            case "ASK_MAIN_RESULT" -> language == UserLanguage.EN ? "What worked today?" : "Что сегодня получилось?";
            case "ASK_MAIN_BLOCKER" -> language == UserLanguage.EN ? "What got in the way of your rhythm?" : "Что мешало ритму?";
            case "ASK_TOMORROW_FOCUS" -> language == UserLanguage.EN
                    ? "What should be simplified or taken into focus tomorrow?"
                    : "Что стоит упростить или взять в фокус завтра?";
            default -> language == UserLanguage.EN ? "Continue the evening reflection." : "Продолжим вечернюю рефлексию.";
        };
    }

    private String invalidScore(UserLanguage language) {
        return language == UserLanguage.EN ? "Use a number from 1 to 10." : "Нужно число от 1 до 10.";
    }

    private String dayPlanEmptyState(UserLanguage language) {
        return language == UserLanguage.EN ? """
                The day plan works better after a check-in.

                You can start with a short state check or get a minimal plan now.
                """ : """
                Плана будет больше смысла после check-in.

                Можно начать с короткой оценки состояния или получить минимальный план сейчас.
                """;
    }

    private String reportEmptyState(UserLanguage language) {
        return language == UserLanguage.EN ? """
                There is not enough data for a useful report yet.

                To make the report helpful, complete a few check-ins, track habits and return for evening reflection.
                """ : """
                Пока мало данных для отчёта.

                Чтобы отчёт стал полезным, сделай несколько check-in, отметь привычки и вернись к вечерней рефлексии.
                """;
    }

    private String habitEmptyState(UserLanguage language) {
        return language == UserLanguage.EN ? """
                There is no active habit for today yet.

                Choose one small habit that is realistic even on a busy day.

                Which habit should we track today?
                """ : """
                Пока нет активной привычки на сегодня.

                Выбери одну маленькую привычку, которую реально сделать даже в загруженный день.

                Какую одну привычку сегодня отслеживаем?
                """;
    }

    private boolean hasCheckIns(TelegramUserEntity user) {
        try {
            return dayPlanService.hasCheckIns(user);
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private boolean hasUsefulReportData(TelegramUserEntity user) {
        try {
            return weeklyReportService.hasUsefulData(user);
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private boolean hasRecentHabits(TelegramUserEntity user) {
        try {
            return habitService.hasRecent(user, Instant.now().minus(1, ChronoUnit.DAYS));
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private boolean isCommand(String text) {
        return text != null && text.strip().startsWith("/");
    }

    private String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.strip();
    }

    private String areaText(LifeArea area, UserLanguage language) {
        if (area == null) {
            return language == UserLanguage.EN ? "not selected" : "не выбран";
        }
        return switch (area) {
            case DAILY_STRUCTURE -> language == UserLanguage.EN ? "Daily rhythm" : "Режим дня";
            case FOCUS -> language == UserLanguage.EN ? "Focus and tasks" : "Фокус и задачи";
            case HABITS -> language == UserLanguage.EN ? "Habits" : "Привычки";
            case ENERGY -> language == UserLanguage.EN ? "Energy" : "Энергия";
            case RECOVERY -> language == UserLanguage.EN ? "Recovery" : "Восстановление";
            case MOVEMENT -> language == UserLanguage.EN ? "Movement" : "Движение";
            case NUTRITION -> language == UserLanguage.EN ? "Nutrition" : "Питание";
            case GENERAL_BALANCE -> language == UserLanguage.EN ? "General balance" : "Баланс";
        };
    }

    private String planningStyleText(PlanningStyle style, UserLanguage language) {
        if (style == null) {
            return language == UserLanguage.EN ? "not selected" : "не выбран";
        }
        return switch (style) {
            case MINIMAL -> language == UserLanguage.EN ? "Minimal" : "Минимальный";
            case BALANCED -> language == UserLanguage.EN ? "Balanced" : "Сбалансированный";
            case DETAILED -> language == UserLanguage.EN ? "Detailed" : "Подробный";
        };
    }

    private String lifeLoops(LifeProfileEntity profile, UserLanguage language) {
        Map<String, Boolean> loops = new LinkedHashMap<>();
        loops.put(language == UserLanguage.EN ? "sleep" : "сон", profile.isSleepFocus());
        loops.put(language == UserLanguage.EN ? "energy/stress" : "энергия/стресс", profile.isStressFocus());
        loops.put(language == UserLanguage.EN ? "habits" : "привычки", profile.isHabitFocus());
        loops.put(language == UserLanguage.EN ? "nutrition" : "питание", profile.isNutritionFocus());
        loops.put(language == UserLanguage.EN ? "movement" : "движение", profile.isMovementFocus());
        loops.put(language == UserLanguage.EN ? "focus" : "фокус", profile.isFocusTasks());
        String selected = loops.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .reduce((left, right) -> left + ", " + right)
                .orElse(language == UserLanguage.EN ? "not selected" : "не выбраны");
        return selected;
    }

    private UserLanguage language(TelegramUserEntity user) {
        return user == null ? UserLanguage.RU : user.getLanguage().orElse(UserLanguage.RU);
    }

    public record FlowResult(String content, RequestType requestType, InlineKeyboardMarkup replyMarkup) {
        public FlowResult(String content, RequestType requestType) {
            this(content, requestType, null);
        }
    }

    private record StepBack(String step, String... keysToRemove) {
    }
}
