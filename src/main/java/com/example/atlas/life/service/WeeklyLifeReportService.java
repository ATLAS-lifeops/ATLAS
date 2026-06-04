package com.example.atlas.life.service;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.service.EveningReflectionService;
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
@ConditionalOnBean({LifeProfileService.class, CheckInRepository.class, HabitService.class, EveningReflectionService.class})
public class WeeklyLifeReportService {

    private final LifeProfileService lifeProfileService;
    private final CheckInRepository checkInRepository;
    private final HabitService habitService;
    private final EveningReflectionService reflectionService;
    private final Clock clock;

    public WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService
    ) {
        this(lifeProfileService, checkInRepository, habitService, reflectionService, Clock.systemUTC());
    }

    WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            Clock clock
    ) {
        this.lifeProfileService = lifeProfileService;
        this.checkInRepository = checkInRepository;
        this.habitService = habitService;
        this.reflectionService = reflectionService;
        this.clock = clock;
    }

    @Transactional
    public String weeklyReport(TelegramUserEntity user) {
        Instant since = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        LifeProfileEntity profile = lifeProfileService.getOrCreate(user);
        List<CheckInEntity> checkIns = checkInRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
        List<HabitCheckEntity> habits = habitService.recent(user, since);
        List<EveningReflectionEntity> reflections = reflectionService.recent(user, since);

        return """
                Недельный отчёт

                Check-ins: %d из 7
                Средняя энергия: %s
                Средний фокус: %s
                Средний стресс: %s
                Сон: %s

                Привычки:
                %s

                Рефлексия:
                %s

                Главный паттерн:
                %s

                Фокус следующей недели:
                %s
                """.formatted(
                checkIns.size(),
                averageText(checkIns.stream().map(CheckInEntity::getEnergy).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getFocus).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getStress).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getSleepQuality).toList()),
                habitSummary(habits),
                reflectionSummary(reflections),
                pattern(checkIns),
                nextFocus(profile, reflections)
        );
    }

    @Transactional(readOnly = true)
    public boolean hasUsefulData(TelegramUserEntity user) {
        Instant since = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        return !checkInRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since).isEmpty()
                || !habitService.recent(user, since).isEmpty()
                || !reflectionService.recent(user, since).isEmpty();
    }

    private String averageText(List<Integer> values) {
        OptionalDouble average = values.stream()
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? String.format("%.1f/10", average.getAsDouble()) : "нет данных";
    }

    private String habitSummary(List<HabitCheckEntity> habits) {
        if (habits.isEmpty()) {
            return "нет сохранённых привычек за 7 дней";
        }
        long completed = habits.stream().filter(HabitCheckEntity::isCompleted).count();
        String latest = habits.getFirst().getHabitName();
        return "%d из %d выполнено; последняя привычка: %s".formatted(completed, habits.size(), latest);
    }

    private String reflectionSummary(List<EveningReflectionEntity> reflections) {
        if (reflections.isEmpty()) {
            return "нет вечерних рефлексий за 7 дней";
        }
        EveningReflectionEntity latest = reflections.getFirst();
        return "последний результат: %s; фокус на завтра: %s".formatted(
                orMissing(latest.getMainResult()),
                orMissing(latest.getTomorrowFocus())
        );
    }

    private String pattern(List<CheckInEntity> checkIns) {
        if (checkIns.isEmpty()) {
            return "нужно больше check-ins, чтобы увидеть паттерн";
        }
        boolean overload = checkIns.stream().anyMatch(checkIn -> checkIn.isOverloadFlag() || checkIn.isPainFlag());
        if (overload) {
            return "были отметки перегруза или боли; план стоит держать легче и не игнорировать серьёзные симптомы";
        }
        double stress = average(checkIns.stream().map(CheckInEntity::getStress).toList());
        double energy = average(checkIns.stream().map(CheckInEntity::getEnergy).toList());
        if (stress >= 7) {
            return "стресс часто высокий, план лучше упрощать заранее";
        }
        if (energy > 0 && energy <= 4) {
            return "энергия часто низкая, день лучше собирать через минимум обязательного";
        }
        return "регулярность данных важнее плотности плана";
    }

    private String nextFocus(LifeProfileEntity profile, List<EveningReflectionEntity> reflections) {
        if (!reflections.isEmpty() && reflections.getFirst().getTomorrowFocus() != null && !reflections.getFirst().getTomorrowFocus().isBlank()) {
            return reflections.getFirst().getTomorrowFocus();
        }
        if (profile.getCurrentFocus() != null && !profile.getCurrentFocus().isBlank()) {
            return profile.getCurrentFocus();
        }
        return "держать короткий ежедневный check-in и одну минимальную привычку";
    }

    private double average(List<Integer> values) {
        return values.stream()
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private String orMissing(String value) {
        return value == null || value.isBlank() ? "нет данных" : value;
    }
}
