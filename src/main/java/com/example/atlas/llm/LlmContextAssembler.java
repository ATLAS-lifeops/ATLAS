package com.example.atlas.llm;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.repository.HabitCheckRepository;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.repository.LifeProfileRepository;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.repository.EveningReflectionRepository;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

@Service
@ConditionalOnBean({
        LifeProfileRepository.class,
        CheckInRepository.class,
        HabitCheckRepository.class,
        EveningReflectionRepository.class
})
public class LlmContextAssembler {

    private final LifeProfileService lifeProfileService;
    private final CheckInRepository checkInRepository;
    private final HabitService habitService;
    private final EveningReflectionService reflectionService;
    private final SafetyGuard safetyGuard;
    private final Clock clock;

    public LlmContextAssembler(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            SafetyGuard safetyGuard
    ) {
        this(lifeProfileService, checkInRepository, habitService, reflectionService, safetyGuard, Clock.systemUTC());
    }

    LlmContextAssembler(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            SafetyGuard safetyGuard,
            Clock clock
    ) {
        this.lifeProfileService = lifeProfileService;
        this.checkInRepository = checkInRepository;
        this.habitService = habitService;
        this.reflectionService = reflectionService;
        this.safetyGuard = safetyGuard;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PromptContext assemble(TelegramUserEntity user, PromptPurpose purpose, String currentRequest) {
        UserLanguage language = user.getLanguage().orElse(UserLanguage.RU);
        LifeProfileEntity profile = lifeProfileService.find(user).orElse(null);
        Instant since = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        List<CheckInEntity> checkIns = checkInRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
        CheckInEntity latest = checkIns.isEmpty()
                ? checkInRepository.findByTelegramUserOrderByCreatedAtDesc(user).stream().findFirst().orElse(null)
                : checkIns.getFirst();
        List<HabitCheckEntity> habits = habitService.recent(user, since);
        List<EveningReflectionEntity> reflections = reflectionService.recent(user, since);
        boolean safetyRisk = safetyGuard.requiresSafetyResponse(currentRequest)
                || checkIns.stream().anyMatch(checkIn -> checkIn.isPainFlag() || checkIn.isOverloadFlag());

        String context = """
                User profile
                %s

                Latest check-in
                %s

                Recent patterns
                %s

                Habits
                %s

                Reflections
                %s

                Current request
                %s

                Safety notes
                %s
                """.formatted(
                profileText(profile, language),
                latestCheckInText(latest),
                recentPatternText(checkIns),
                habitText(habits),
                reflectionText(reflections),
                blank(currentRequest) ? "not provided" : currentRequest.strip(),
                safetyRisk ? "Risk words or saved risk flags are present." : "No explicit safety risk detected."
        ).strip();

        return new PromptContext(purpose, user.getId(), language, context, currentRequest, safetyRisk);
    }

    private String profileText(LifeProfileEntity profile, UserLanguage language) {
        if (profile == null) {
            return "not available";
        }
        return "language=%s; onboarding=%s; primary_life_area=%s; current_focus=%s; planning_style=%s; loops=%s".formatted(
                language.code(),
                profile.isOnboardingCompleted() ? "completed" : "not completed",
                value(profile.getPrimaryLifeArea()),
                value(profile.getCurrentFocus()),
                value(profile.getPlanningStyle()),
                loops(profile)
        );
    }

    private String latestCheckInText(CheckInEntity latest) {
        if (latest == null) {
            return "not available";
        }
        return "energy=%s; focus=%s; stress=%s; sleep=%s; mood=%s; priority=%s; overload=%s; pain=%s; notes=%s".formatted(
                value(latest.getEnergy()),
                value(latest.getFocus()),
                value(latest.getStress()),
                value(latest.getSleepQuality()),
                value(latest.getMood()),
                value(latest.getMainPriority()),
                latest.isOverloadFlag(),
                latest.isPainFlag(),
                value(latest.getNotes())
        );
    }

    private String recentPatternText(List<CheckInEntity> checkIns) {
        if (checkIns.isEmpty()) {
            return "no check-ins in the last 7 days";
        }
        long riskCount = checkIns.stream().filter(checkIn -> checkIn.isOverloadFlag() || checkIn.isPainFlag()).count();
        return "check_ins=%d; avg_energy=%s; avg_focus=%s; avg_stress=%s; avg_sleep=%s; risk_flags=%d".formatted(
                checkIns.size(),
                average(checkIns.stream().map(CheckInEntity::getEnergy).toList()),
                average(checkIns.stream().map(CheckInEntity::getFocus).toList()),
                average(checkIns.stream().map(CheckInEntity::getStress).toList()),
                average(checkIns.stream().map(CheckInEntity::getSleepQuality).toList()),
                riskCount
        );
    }

    private String habitText(List<HabitCheckEntity> habits) {
        if (habits.isEmpty()) {
            return "no habit data in the last 7 days";
        }
        long completed = habits.stream().filter(HabitCheckEntity::isCompleted).count();
        String latest = habits.getFirst().getHabitName();
        return "records=%d; completed=%d; latest_habit=%s; latest_minimum=%s".formatted(
                habits.size(),
                completed,
                value(latest),
                value(habits.getFirst().getMinimumVersion())
        );
    }

    private String reflectionText(List<EveningReflectionEntity> reflections) {
        if (reflections.isEmpty()) {
            return "no evening reflections in the last 7 days";
        }
        EveningReflectionEntity latest = reflections.getFirst();
        return "records=%d; latest_result=%s; latest_blocker=%s; tomorrow_focus=%s".formatted(
                reflections.size(),
                value(latest.getMainResult()),
                value(latest.getMainBlocker()),
                value(latest.getTomorrowFocus())
        );
    }

    private String loops(LifeProfileEntity profile) {
        java.util.List<String> values = new java.util.ArrayList<>();
        if (profile.isSleepFocus()) {
            values.add("sleep");
        }
        if (profile.isStressFocus()) {
            values.add("stress");
        }
        if (profile.isHabitFocus()) {
            values.add("habits");
        }
        if (profile.isNutritionFocus()) {
            values.add("nutrition");
        }
        if (profile.isMovementFocus()) {
            values.add("movement");
        }
        if (profile.isFocusTasks()) {
            values.add("focus_tasks");
        }
        return values.isEmpty() ? "not selected" : String.join(",", values);
    }

    private String average(List<Integer> values) {
        OptionalDouble average = values.stream()
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? "%.1f/10".formatted(average.getAsDouble()) : "not available";
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "not available" : value.toString();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
