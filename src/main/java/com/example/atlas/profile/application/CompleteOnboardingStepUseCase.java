package com.example.atlas.profile.application;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(TelegramLifeFlowService.class)
public class CompleteOnboardingStepUseCase implements UseCase<CompleteOnboardingStepUseCase.Input, TelegramLifeFlowService.FlowResult> {

    private final TelegramLifeFlowService flowService;

    public CompleteOnboardingStepUseCase(TelegramLifeFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public TelegramLifeFlowService.FlowResult execute(Input input) {
        return flowService.handle(input.user(), input.answer(), RequestType.GENERAL).orElseThrow();
    }

    public record Input(TelegramUserEntity user, String answer) implements Command {
    }
}
