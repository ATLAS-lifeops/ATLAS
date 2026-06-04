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
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.telegram.InlineKeyboardMarkup;
import com.example.atlas.telegram.TelegramKeyboardFactory;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        this.conversationStateService = conversationStateService;
        this.lifeProfileService = lifeProfileService;
        this.checkInPersistenceService = checkInPersistenceService;
        this.habitService = habitService;
        this.reflectionService = reflectionService;
        this.dayPlanService = dayPlanService;
        this.weeklyReportService = weeklyReportService;
        this.safetyGuard = safetyGuard;
        this.keyboardFactory = keyboardFactory;
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
                new TelegramKeyboardFactory()
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
            conversationStateService.active(user).ifPresent(conversationStateService::cancel);
            return Optional.of(new FlowResult(dayPlanService.dayPlan(user), requestType, keyboardFactory.dayPlanActions()));
        }
        if (requestType == RequestType.HABITS) {
            return Optional.of(startFlow(user, ConversationFlowType.HABIT_TRACKING, "ASK_HABIT", "Какую одну привычку сегодня отслеживаем?"));
        }
        if (requestType == RequestType.EVENING_REFLECTION) {
            return Optional.of(startFlow(user, ConversationFlowType.EVENING_REFLECTION, "ASK_MAIN_RESULT", "Что сегодня получилось?"));
        }
        if (requestType == RequestType.REPORT) {
            conversationStateService.active(user).ifPresent(conversationStateService::cancel);
            return Optional.of(new FlowResult(weeklyReportService.weeklyReport(user), requestType, keyboardFactory.reportActions()));
        }
        if (requestType == RequestType.EMERGENCY) {
            conversationStateService.active(user).ifPresent(conversationStateService::cancel);
            return Optional.of(new FlowResult(emergency(text), requestType, keyboardFactory.backToMenu()));
        }

        if (!isCommand(text)) {
            Optional<ConversationStateEntity> active = conversationStateService.active(user);
            if (active.isPresent()) {
                return Optional.of(continueFlow(active.get(), text));
            }
        }

        return Optional.empty();
    }

    private FlowResult startOnboardingOrWelcomeBack(TelegramUserEntity user) {
        conversationStateService.active(user).ifPresent(conversationStateService::cancel);
        LifeProfileEntity profile = lifeProfileService.getOrCreate(user);
        if (profile.isOnboardingCompleted()) {
            return new FlowResult(
                    "ATLAS\n\nЧто хочешь сделать сейчас?",
                    RequestType.START,
                    keyboardFactory.mainMenu()
            );
        }
        conversationStateService.start(user, ConversationFlowType.ONBOARDING, "ASK_PRIMARY_LIFE_AREA");
        return new FlowResult(onboardingIntro(), RequestType.START, keyboardFactory.onboardingLifeAreas());
    }

    private FlowResult handleCheckinCommand(TelegramUserEntity user, String text) {
        if (hasStructuredCheckinValues(text)) {
            checkInPersistenceService.record(user, text);
            String safety = safetyGuard.requiresSafetyResponse(text) ? "\n\n" + safetyGuard.safetyResponse() : "";
            return new FlowResult("Check-in сохранён. Используй /day, чтобы собрать реалистичный план дня." + safety, RequestType.CHECKIN, keyboardFactory.dayPlanActions());
        }
        return startFlow(user, ConversationFlowType.DAILY_CHECKIN, "ASK_ENERGY", "Оцени энергию от 1 до 10.");
    }

    private FlowResult startFlow(TelegramUserEntity user, ConversationFlowType flowType, String step, String prompt) {
        ConversationStateEntity state = conversationStateService.start(user, flowType, step);
        return new FlowResult(prompt, requestType(flowType), keyboardFactory.forActiveStep(state));
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
        Instant now = Instant.now();
        switch (state.getStep()) {
            case "ASK_PRIMARY_LIFE_AREA" -> {
                LifeArea area = parseLifeArea(text);
                profile.updatePrimaryLifeArea(area, now);
                payload.put("primary_life_area", area.name());
                conversationStateService.moveTo(state, "ASK_CURRENT_FOCUS", payload);
                return new FlowResult("Что сейчас важнее всего привести в порядок? Ответь коротко одной фразой.", RequestType.START, keyboardFactory.forActiveStep(state));
            }
            case "ASK_CURRENT_FOCUS" -> {
                payload.put("current_focus", clean(text));
                profile.updateCurrentFocus(clean(text), now);
                conversationStateService.moveTo(state, "ASK_PLANNING_STYLE", payload);
                return new FlowResult("""
                        Какой стиль плана тебе ближе?
                        1 - Минимальный
                        2 - Сбалансированный
                        3 - Подробный
                        """, RequestType.START, keyboardFactory.planningStyles());
            }
            case "ASK_PLANNING_STYLE" -> {
                PlanningStyle style = parsePlanningStyle(text);
                payload.put("planning_style", style.name());
                profile.updatePlanningStyle(style, now);
                conversationStateService.moveTo(state, "ASK_MAIN_LOOPS", payload);
                return new FlowResult("""
                        Какие контуры жизни сейчас особенно важны? Можно выбрать несколько цифр:
                        1 - Сон
                        2 - Энергия / стресс
                        3 - Привычки
                        4 - Питание
                        5 - Движение
                        6 - Фокус и задачи
                        """, RequestType.START, keyboardFactory.forActiveStep(state));
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
                return new FlowResult("Готово. Я сохранил профиль. Начнём с короткого check-in.", RequestType.START, keyboardFactory.mainMenu());
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_PRIMARY_LIFE_AREA", payload);
                return new FlowResult(onboardingIntro(), RequestType.START, keyboardFactory.onboardingLifeAreas());
            }
        }
    }

    private FlowResult continueDailyCheckin(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        switch (state.getStep()) {
            case "ASK_ENERGY" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult("Нужно число от 1 до 10. Оцени энергию от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("energy"));
                }
                payload.put("energy", value.toString());
                conversationStateService.moveTo(state, "ASK_FOCUS", payload);
                return new FlowResult("Оцени фокус от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("focus"));
            }
            case "ASK_FOCUS" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult("Нужно число от 1 до 10. Оцени фокус от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("focus"));
                }
                payload.put("focus", value.toString());
                conversationStateService.moveTo(state, "ASK_STRESS", payload);
                return new FlowResult("Оцени стресс от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("stress"));
            }
            case "ASK_STRESS" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult("Нужно число от 1 до 10. Оцени стресс от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("stress"));
                }
                payload.put("stress", value.toString());
                conversationStateService.moveTo(state, "ASK_SLEEP", payload);
                return new FlowResult("Оцени сон от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("sleep"));
            }
            case "ASK_SLEEP" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult("Нужно число от 1 до 10. Оцени сон от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("sleep"));
                }
                payload.put("sleep", value.toString());
                conversationStateService.moveTo(state, "ASK_MOOD", payload);
                return new FlowResult("Оцени настроение от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("mood"));
            }
            case "ASK_MOOD" -> {
                Integer value = parseScore(text);
                if (value == null) {
                    return new FlowResult("Нужно число от 1 до 10. Оцени настроение от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("mood"));
                }
                payload.put("mood", value.toString());
                conversationStateService.moveTo(state, "ASK_MAIN_PRIORITY", payload);
                return new FlowResult("Что сегодня главное?", RequestType.CHECKIN, keyboardFactory.forActiveStep(state));
            }
            case "ASK_MAIN_PRIORITY" -> {
                payload.put("main_priority", clean(text));
                conversationStateService.moveTo(state, "ASK_OVERLOAD_SIGNAL", payload);
                return new FlowResult("Есть ли боль, сильный перегруз или тревожный симптом? Ответь да/нет и добавь пару слов, если нужно.", RequestType.CHECKIN, keyboardFactory.yesNo("atlas:checkin:overload:yes", "atlas:checkin:overload:no"));
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
                return new FlowResult("""
                        Check-in сохранён.
                        Энергия: %s/10, фокус: %s/10, стресс: %s/10, сон: %s/10, настроение: %s/10.
                        Главное сегодня: %s.
                        """.formatted(
                        payload.get("energy"),
                        payload.get("focus"),
                        payload.get("stress"),
                        payload.get("sleep"),
                        payload.get("mood"),
                        payload.get("main_priority")
                ).strip() + safety, RequestType.CHECKIN, keyboardFactory.dayPlanActions());
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_ENERGY", payload);
                return new FlowResult("Оцени энергию от 1 до 10.", RequestType.CHECKIN, keyboardFactory.score("energy"));
            }
        }
    }

    private FlowResult continueHabitTracking(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        switch (state.getStep()) {
            case "ASK_HABIT" -> {
                payload.put("habit", clean(text));
                conversationStateService.moveTo(state, "ASK_MINIMUM_VERSION", payload);
                return new FlowResult("Какая минимальная версия этой привычки займёт 2-5 минут?", RequestType.HABITS, keyboardFactory.forActiveStep(state));
            }
            case "ASK_MINIMUM_VERSION" -> {
                payload.put("minimum_version", clean(text));
                conversationStateService.moveTo(state, "ASK_COMPLETION", payload);
                return new FlowResult("Сегодня она уже выполнена? Да/нет.", RequestType.HABITS, keyboardFactory.yesNo("atlas:habit:completed:yes", "atlas:habit:completed:no"));
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
                return new FlowResult("Привычка сохранена. Минимальная версия: " + payload.get("minimum_version") + ".", RequestType.HABITS, keyboardFactory.habitCompleteActions());
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_HABIT", payload);
                return new FlowResult("Какую одну привычку сегодня отслеживаем?", RequestType.HABITS, keyboardFactory.forActiveStep(state));
            }
        }
    }

    private FlowResult continueEveningReflection(ConversationStateEntity state, String text) {
        Map<String, String> payload = conversationStateService.payload(state);
        switch (state.getStep()) {
            case "ASK_MAIN_RESULT" -> {
                payload.put("main_result", clean(text));
                conversationStateService.moveTo(state, "ASK_MAIN_BLOCKER", payload);
                return new FlowResult("Что мешало ритму?", RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state));
            }
            case "ASK_MAIN_BLOCKER" -> {
                payload.put("main_blocker", clean(text));
                conversationStateService.moveTo(state, "ASK_TOMORROW_FOCUS", payload);
                return new FlowResult("Что стоит упростить или взять в фокус завтра?", RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state));
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
                return new FlowResult("Рефлексия сохранена. Завтра держим фокус: " + payload.get("tomorrow_focus") + ".", RequestType.EVENING_REFLECTION, keyboardFactory.eveningCompleteActions());
            }
            default -> {
                conversationStateService.moveTo(state, "ASK_MAIN_RESULT", payload);
                return new FlowResult("Что сегодня получилось?", RequestType.EVENING_REFLECTION, keyboardFactory.forActiveStep(state));
            }
        }
    }

    private FlowResult cancel(TelegramUserEntity user) {
        Optional<ConversationStateEntity> active = conversationStateService.active(user);
        active.ifPresent(conversationStateService::cancel);
        String text = active.isPresent()
                ? "Текущий сценарий отменён. Можно начать заново: /checkin, /day, /habits, /evening или /report."
                : "Активного сценария нет. Можно начать: /checkin, /day, /habits, /evening или /report.";
        return new FlowResult(text, RequestType.CANCEL, keyboardFactory.backToMenu());
    }

    @Transactional(readOnly = true)
    public String settings(TelegramUserEntity user) {
        Optional<LifeProfileEntity> profile = lifeProfileService.find(user);
        if (profile.isEmpty()) {
            return """
                    Настройки ATLAS

                    Onboarding: нет
                    Профиль ещё не заполнен.
                    """;
        }
        LifeProfileEntity value = profile.get();
        return """
                Настройки ATLAS

                Onboarding: %s
                Главная область: %s
                Стиль планирования: %s
                Контуры: %s
                """.formatted(
                value.isOnboardingCompleted() ? "да" : "нет",
                value.getPrimaryLifeArea() == null ? "не выбрана" : value.getPrimaryLifeArea().name(),
                value.getPlanningStyle() == null ? "не выбран" : value.getPlanningStyle().name(),
                lifeLoops(value)
        ).strip();
    }

    @Transactional
    public FlowResult restartOnboarding(TelegramUserEntity user) {
        conversationStateService.active(user).ifPresent(conversationStateService::cancel);
        lifeProfileService.getOrCreate(user);
        conversationStateService.start(user, ConversationFlowType.ONBOARDING, "ASK_PRIMARY_LIFE_AREA");
        return new FlowResult(onboardingIntro(), RequestType.START, keyboardFactory.onboardingLifeAreas());
    }

    private String onboardingIntro() {
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

    private String emergency(String text) {
        String safety = safetyGuard.requiresSafetyResponse(text) ? "\n\n" + safetyGuard.safetyResponse() : "";
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

    private boolean isCommand(String text) {
        return text != null && text.strip().startsWith("/");
    }

    private String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.strip();
    }

    private String lifeLoops(LifeProfileEntity profile) {
        Map<String, Boolean> loops = new LinkedHashMap<>();
        loops.put("сон", profile.isSleepFocus());
        loops.put("энергия/стресс", profile.isStressFocus());
        loops.put("привычки", profile.isHabitFocus());
        loops.put("питание", profile.isNutritionFocus());
        loops.put("движение", profile.isMovementFocus());
        loops.put("фокус", profile.isStressFocus());
        String selected = loops.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .reduce((left, right) -> left + ", " + right)
                .orElse("не выбраны");
        return selected;
    }

    public record FlowResult(String content, RequestType requestType, InlineKeyboardMarkup replyMarkup) {
        public FlowResult(String content, RequestType requestType) {
            this(content, requestType, null);
        }
    }
}
