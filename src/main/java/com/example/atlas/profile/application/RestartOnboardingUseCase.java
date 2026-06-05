package com.example.atlas.profile.application;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(TelegramLifeFlowService.class)
public class RestartOnboardingUseCase implements UseCase<RestartOnboardingUseCase.Input, TelegramLifeFlowService.FlowResult> {

    private final TelegramLifeFlowService flowService;

    public RestartOnboardingUseCase(TelegramLifeFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public TelegramLifeFlowService.FlowResult execute(Input input) {
        return flowService.restartOnboarding(input.user());
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
