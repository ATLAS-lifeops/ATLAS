package com.example.atlas.tracking.application;

import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.habit.service.HabitService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.HabitTrackedEvent;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TrackHabitUseCase implements UseCase<TrackHabitUseCase.Input, HabitCheckEntity> {

    private final ObjectProvider<HabitService> habitService;
    private final EventPublisher eventPublisher;

    public TrackHabitUseCase(ObjectProvider<HabitService> habitService, EventPublisher eventPublisher) {
        this.habitService = habitService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public HabitCheckEntity execute(Input input) {
        HabitCheckEntity entity = requireHabitService()
                .record(input.user(), input.habitName(), input.minimumVersion(), input.completed(), input.notes());
        eventPublisher.publish(new HabitTrackedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                input.habitName(),
                input.completed(),
                Instant.now()
        ));
        return entity;
    }

    private HabitService requireHabitService() {
        HabitService service = habitService.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("Habit service is not available.");
        }
        return service;
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
