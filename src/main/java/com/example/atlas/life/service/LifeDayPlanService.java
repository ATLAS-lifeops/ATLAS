package com.example.atlas.life.service;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.life.PlanningStyle;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.llm.LlmDayPlanService;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@ConditionalOnBean({LifeProfileService.class, CheckInRepository.class})
public class LifeDayPlanService {

    private final LifeProfileService lifeProfileService;
    private final CheckInRepository checkInRepository;
    private final ObjectProvider<LlmDayPlanService> llmDayPlanService;

    public LifeDayPlanService(LifeProfileService lifeProfileService, CheckInRepository checkInRepository) {
        this(lifeProfileService, checkInRepository, null);
    }

    @Autowired
    public LifeDayPlanService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            ObjectProvider<LlmDayPlanService> llmDayPlanService
    ) {
        this.lifeProfileService = lifeProfileService;
        this.checkInRepository = checkInRepository;
        this.llmDayPlanService = llmDayPlanService;
    }

    @Transactional
    public String dayPlan(TelegramUserEntity user) {
        String deterministic = deterministicDayPlan(user);
        LlmDayPlanService service = llmDayPlanService == null ? null : llmDayPlanService.getIfAvailable();
        if (service == null) {
            return deterministic;
        }
        return service.dayPlan(user, deterministic).orElse(deterministic);
    }

    private String deterministicDayPlan(TelegramUserEntity user) {
        LifeProfileEntity profile = lifeProfileService.getOrCreate(user);
        CheckInEntity latest = checkInRepository.findByTelegramUserOrderByCreatedAtDesc(user).stream()
                .findFirst()
                .orElse(null);

        String onboarding = profile.isOnboardingCompleted()
                ? ""
                : "Онбординг ещё не завершён. Можно пройти /start, а пока держим минимальный план.\n\n";
        int taskCount = taskCount(profile, latest);
        String priority = latest != null && latest.getMainPriority() != null && !latest.getMainPriority().isBlank()
                ? latest.getMainPriority()
                : defaultFocus(profile);
        String stateSupport = stateSupport(latest);
        String minimalHabit = minimalHabit(profile);
        String fallback = latest != null && (latest.isOverloadFlag() || latest.isPainFlag())
                ? "оставить только обязательное и не усиливать нагрузку; при серьёзных симптомах обратиться к квалифицированному специалисту"
                : "выбрать один самый маленький следующий шаг и вернуться к /evening вечером";

        return onboarding + """
                План дня

                1. Главный фокус
                   %s.

                2. Короткий список действий
                   %s

                3. Поддержка состояния
                   %s.

                4. Минимальная привычка
                   %s.

                5. Если день развалится
                   %s.
                """.formatted(
                cleanSentence(priority),
                actionList(taskCount),
                stateSupport,
                minimalHabit,
                fallback
        );
    }

    @Transactional(readOnly = true)
    public boolean hasCheckIns(TelegramUserEntity user) {
        return !checkInRepository.findByTelegramUserOrderByCreatedAtDesc(user).isEmpty();
    }

    private int taskCount(LifeProfileEntity profile, CheckInEntity latest) {
        int count = profile.getPlanningStyle() == PlanningStyle.DETAILED ? 3 : 2;
        if (profile.getPlanningStyle() == PlanningStyle.MINIMAL) {
            count = 1;
        }
        if (latest != null) {
            if (low(latest.getEnergy()) || low(latest.getFocus()) || low(latest.getSleepQuality()) || high(latest.getStress())) {
                count = Math.min(count, 1);
            }
        }
        return count;
    }

    private String actionList(int taskCount) {
        if (taskCount <= 1) {
            return "1 обязательное действие, затем пауза и проверка состояния";
        }
        if (taskCount == 2) {
            return "1 обязательное действие и 1 поддерживающая задача";
        }
        return "1 главный результат и до 2 поддерживающих задач";
    }

    private String stateSupport(CheckInEntity latest) {
        if (latest == null) {
            return "короткие паузы между задачами и спокойный темп";
        }
        if (latest.isOverloadFlag() || latest.isPainFlag()) {
            return "облегчить день, убрать необязательное и не игнорировать серьёзные сигналы";
        }
        if (low(latest.getSleepQuality())) {
            return "спокойный ритм, меньше переключений и без лишнего давления";
        }
        if (high(latest.getStress())) {
            return "снизить плотность плана и оставить запас времени";
        }
        if (low(latest.getFocus())) {
            return "работать короткими блоками и фиксировать только следующий шаг";
        }
        return "держать реалистичный темп и сверяться с состоянием";
    }

    private String minimalHabit(LifeProfileEntity profile) {
        if (profile.isHabitFocus()) {
            return "2-5 минут на одну выбранную привычку";
        }
        if (profile.isSleepFocus()) {
            return "один маленький вечерний шаг для восстановления";
        }
        if (profile.isNutritionFocus()) {
            return "один простой приём еды или подготовка воды";
        }
        if (profile.isMovementFocus()) {
            return "мягкое движение как поддержка состояния, если оно уместно";
        }
        return "один короткий порядок в дне: заметка, пауза или бытовой шаг";
    }

    private String defaultFocus(LifeProfileEntity profile) {
        if (profile.getCurrentFocus() != null && !profile.getCurrentFocus().isBlank()) {
            return profile.getCurrentFocus();
        }
        return "собрать день в управляемый ритм";
    }

    private boolean low(Integer value) {
        return value != null && value <= 4;
    }

    private boolean high(Integer value) {
        return value != null && value >= 7;
    }

    private String cleanSentence(String value) {
        String trimmed = value == null ? "" : value.strip();
        if (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isBlank() ? "собрать день в управляемый ритм" : trimmed;
    }
}
