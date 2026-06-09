package com.example.atlas.planning;

import com.example.atlas.planning.entity.WeeklyFocusEntity;
import com.example.atlas.planning.repository.WeeklyFocusRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@ConditionalOnBean(WeeklyFocusRepository.class)
public class WeeklyPlanningService {

    private final WeeklyFocusRepository repository;

    public WeeklyPlanningService(WeeklyFocusRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WeeklyFocusEntity saveFocus(TelegramUserEntity user, LocalDate date, String focus) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return repository.save(new WeeklyFocusEntity(UUID.randomUUID(), user, weekStart, focus.strip(), Instant.now()));
    }

    @Transactional(readOnly = true)
    public String currentFocus(TelegramUserEntity user, LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return repository.findByTelegramUserAndWeekStart(user, weekStart).map(WeeklyFocusEntity::getFocus).orElse("");
    }
}
