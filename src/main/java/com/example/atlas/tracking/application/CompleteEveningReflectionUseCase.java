package com.example.atlas.tracking.application;

import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EveningReflectionCompletedEvent;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@ConditionalOnBean(EveningReflectionService.class)
public class CompleteEveningReflectionUseCase implements UseCase<CompleteEveningReflectionUseCase.Input, EveningReflectionEntity> {

    private final EveningReflectionService reflectionService;
    private final EventPublisher eventPublisher;

    public CompleteEveningReflectionUseCase(EveningReflectionService reflectionService, EventPublisher eventPublisher) {
        this.reflectionService = reflectionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EveningReflectionEntity execute(Input input) {
        EveningReflectionEntity entity = reflectionService.record(input.user(), input.mainResult(), input.mainBlocker(), input.tomorrowFocus());
        eventPublisher.publish(new EveningReflectionCompletedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                Instant.now()
        ));
        return entity;
    }

    public record Input(
            TelegramUserEntity user,
            String mainResult,
            String mainBlocker,
            String tomorrowFocus
    ) implements Command {
    }
}
