package com.example.atlas.profile.application;

import com.example.atlas.conversation.service.TelegramLifeFlowService;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class RestartOnboardingUseCase implements UseCase<RestartOnboardingUseCase.Input, TelegramLifeFlowService.FlowResult> {

    private final Supplier<TelegramLifeFlowService> flowService;

    @Autowired
    public RestartOnboardingUseCase(ObjectProvider<TelegramLifeFlowService> flowService) {
        this.flowService = flowService::getIfAvailable;
    }

    public RestartOnboardingUseCase(TelegramLifeFlowService flowService) {
        this(() -> flowService);
    }

    private RestartOnboardingUseCase(Supplier<TelegramLifeFlowService> flowService) {
        this.flowService = flowService;
    }

    @Override
    public TelegramLifeFlowService.FlowResult execute(Input input) {
        return requireFlowService().restartOnboarding(input.user());
    }

    private TelegramLifeFlowService requireFlowService() {
        TelegramLifeFlowService service = flowService.get();
        if (service == null) {
            throw new IllegalStateException("Telegram life flow service is not available.");
        }
        return service;
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
