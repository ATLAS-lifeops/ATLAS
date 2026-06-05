package com.example.atlas.tracking.application;

import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.HabitTrackedEvent;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@ConditionalOnBean(HabitService.class)
public class TrackHabitUseCase implements UseCase<TrackHabitUseCase.Input, HabitCheckEntity> {

    private final HabitService habitService;
    private final EventPublisher eventPublisher;

    public TrackHabitUseCase(HabitService habitService, EventPublisher eventPublisher) {
        this.habitService = habitService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public HabitCheckEntity execute(Input input) {
        HabitCheckEntity entity = habitService.record(input.user(), input.habitName(), input.minimumVersion(), input.completed(), input.notes());
        eventPublisher.publish(new HabitTrackedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                input.habitName(),
                input.completed(),
                Instant.now()
        ));
        return entity;
    }

    public record Input(
            TelegramUserEntity user,
            String habitName,
            String minimumVersion,
            boolean completed,
            String notes
    ) implements Command {
    }
}
