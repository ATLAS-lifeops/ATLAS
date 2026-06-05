package com.example.atlas.setup.application;

import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.SetupCompletedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CompleteSetupUseCase implements UseCase<CompleteSetupUseCase.Input, Void> {

    private final EventPublisher eventPublisher;

    public CompleteSetupUseCase(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(Input input) {
        eventPublisher.publish(new SetupCompletedEvent(Instant.now()));
        return null;
    }

    public record Input() implements Command {
    }
}
