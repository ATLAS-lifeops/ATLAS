package com.example.atlas.tracking.application;

import com.example.atlas.reflection.entity.EveningReflectionEntity;
import com.example.atlas.reflection.service.EveningReflectionService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.domain.TelegramUserId;
import com.example.atlas.shared.events.EveningReflectionCompletedEvent;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CompleteEveningReflectionUseCase implements UseCase<CompleteEveningReflectionUseCase.Input, EveningReflectionEntity> {

    private final ObjectProvider<EveningReflectionService> reflectionService;
    private final EventPublisher eventPublisher;

    public CompleteEveningReflectionUseCase(ObjectProvider<EveningReflectionService> reflectionService, EventPublisher eventPublisher) {
        this.reflectionService = reflectionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EveningReflectionEntity execute(Input input) {
        EveningReflectionEntity entity = requireReflectionService()
                .record(input.user(), input.mainResult(), input.mainBlocker(), input.tomorrowFocus());
        eventPublisher.publish(new EveningReflectionCompletedEvent(
                new TelegramUserId(input.user().getTelegramUserId()),
                Instant.now()
        ));
        return entity;
    }

    private EveningReflectionService requireReflectionService() {
        EveningReflectionService service = reflectionService.getIfAvailable();
        if (service == null) {
            throw new IllegalStateException("Evening reflection service is not available.");
        }
        return service;
    }

    public record Input(
            TelegramUserEntity user,
            String mainResult,
            String mainBlocker,
            String tomorrowFocus
    ) implements Command {
    }
}
