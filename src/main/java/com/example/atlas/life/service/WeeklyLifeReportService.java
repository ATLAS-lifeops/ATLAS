package com.example.atlas.life.service;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.llm.LlmReportSummaryService;
import com.example.atlas.planning.WeeklyPlanningService;
import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.reporting.TrendDetectionService;
import com.example.atlas.reporting.TrendSummary;
import com.example.atlas.reporting.entity.ReportArchiveEntity;
import com.example.atlas.reporting.repository.ReportArchiveRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@ConditionalOnBean({LifeProfileService.class, CheckInRepository.class, HabitService.class, EveningReflectionService.class})
public class WeeklyLifeReportService {

    private final LifeProfileService lifeProfileService;
    private final CheckInRepository checkInRepository;
    private final HabitService habitService;
    private final EveningReflectionService reflectionService;
    private final Clock clock;
    private final ObjectProvider<LlmReportSummaryService> llmReportSummaryService;
    private final ObjectProvider<WeeklyPlanningService> weeklyPlanningService;
    private final ObjectProvider<TrendDetectionService> trendDetectionService;
    private final ObjectProvider<ReportArchiveRepository> reportArchiveRepository;

    public WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService
    ) {
        this(lifeProfileService, checkInRepository, habitService, reflectionService, Clock.systemUTC(), null, null, null, null);
    }

    @Autowired
    public WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            ObjectProvider<LlmReportSummaryService> llmReportSummaryService,
            ObjectProvider<WeeklyPlanningService> weeklyPlanningService,
            ObjectProvider<TrendDetectionService> trendDetectionService,
            ObjectProvider<ReportArchiveRepository> reportArchiveRepository
    ) {
        this(lifeProfileService, checkInRepository, habitService, reflectionService, Clock.systemUTC(), llmReportSummaryService, weeklyPlanningService, trendDetectionService, reportArchiveRepository);
    }

    WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            Clock clock,
            ObjectProvider<LlmReportSummaryService> llmReportSummaryService
    ) {
        this(lifeProfileService, checkInRepository, habitService, reflectionService, clock, llmReportSummaryService, null, null, null);
    }

    WeeklyLifeReportService(
            LifeProfileService lifeProfileService,
            CheckInRepository checkInRepository,
            HabitService habitService,
            EveningReflectionService reflectionService,
            Clock clock,
            ObjectProvider<LlmReportSummaryService> llmReportSummaryService,
            ObjectProvider<WeeklyPlanningService> weeklyPlanningService,
            ObjectProvider<TrendDetectionService> trendDetectionService,
            ObjectProvider<ReportArchiveRepository> reportArchiveRepository
    ) {
        this.lifeProfileService = lifeProfileService;
        this.checkInRepository = checkInRepository;
        this.habitService = habitService;
        this.reflectionService = reflectionService;
        this.clock = clock;
        this.llmReportSummaryService = llmReportSummaryService;
        this.weeklyPlanningService = weeklyPlanningService;
        this.trendDetectionService = trendDetectionService;
        this.reportArchiveRepository = reportArchiveRepository;
    }

    @Transactional
    public String weeklyReport(TelegramUserEntity user) {
        String deterministic = deterministicWeeklyReport(user);
        LlmReportSummaryService service = llmReportSummaryService == null ? null : llmReportSummaryService.getIfAvailable();
        String report = service == null ? deterministic : service.summary(user, deterministic).orElse(deterministic);
        archive(user, report);
        return report;
    }

    private String deterministicWeeklyReport(TelegramUserEntity user) {
        Instant since = Instant.now(clock).minus(7, ChronoUnit.DAYS);
        LifeProfileEntity profile = lifeProfileService.getOrCreate(user);
        List<CheckInEntity> checkIns = checkInRepository.findByTelegramUserAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
        List<HabitCheckEntity> habits = habitService.recent(user, since);
        List<EveningReflectionEntity> reflections = reflectionService.recent(user, since);
        TrendSummary trends = trends(checkIns, habits);

        return """
                Недельный отчёт

                Weekly focus: %s
                Check-ins: %d из 7
                Средняя энергия: %s
                Средний фокус: %s
                Средний стресс: %s
                Сон: %s

                Trends:
                energy=%s, focus=%s, stress=%s, sleep=%s, habits=%s

                Привычки:
                %s

                Рефлексия:
                %s

                Главный паттерн:
                %s

                Фокус следующей недели:
                %s
                """.formatted(
                weeklyFocus(user),
                checkIns.size(),
                averageText(checkIns.stream().map(CheckInEntity::getEnergy).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getFocus).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getStress).toList()),
                averageText(checkIns.stream().map(CheckInEntity::getSleepQuality).toList()),
                trends.energyTrend(),
                trends.focusTrend(),
                trends.stressTrend(),
                trends.sleepTrend(),
                trends.habitConsistency(),
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

    private TrendSummary trends(List<CheckInEntity> checkIns, List<HabitCheckEntity> habits) {
        TrendDetectionService service = trendDetectionService == null ? null : trendDetectionService.getIfAvailable();
        if (service == null) {
            return new TrendSummary("not_available", "not_available", "not_available", "not_available", "not_available");
        }
        return service.summarize(checkIns, habits);
    }

    private String weeklyFocus(TelegramUserEntity user) {
        WeeklyPlanningService service = weeklyPlanningService == null ? null : weeklyPlanningService.getIfAvailable();
        if (service == null) {
            return "not set";
        }
        String focus = service.currentFocus(user, LocalDate.now(clock));
        return focus == null || focus.isBlank() ? "not set" : focus;
    }

    private void archive(TelegramUserEntity user, String report) {
        ReportArchiveRepository repository = reportArchiveRepository == null ? null : reportArchiveRepository.getIfAvailable();
        if (repository == null || user == null || report == null || report.isBlank()) {
            return;
        }
        LocalDate weekStart = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        repository.save(new ReportArchiveEntity(UUID.randomUUID(), user, weekStart, report, Instant.now(clock)));
    }
}
