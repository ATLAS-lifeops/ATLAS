package com.example.atlas.planning.application;

import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.planning.domain.MinimalPlan;
import com.example.atlas.safety.SafetyGuard;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.UserLanguage;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateMinimalPlanUseCase implements UseCase<CreateMinimalPlanUseCase.Input, MinimalPlan> {

    private final SafetyGuard safetyGuard;

    public CreateMinimalPlanUseCase(SafetyGuard safetyGuard) {
        this.safetyGuard = safetyGuard;
    }

    @Override
    public MinimalPlan execute(Input input) {
        UserLanguage language = input.user().getLanguage().orElse(UserLanguage.RU);
        String safety = safetyGuard.requiresSafetyResponse(input.text()) ? "\n\n" + safetyGuard.safetyResponse() : "";
        String content = language == UserLanguage.EN ? """
                Minimal plan

                1. Stop and reduce the day to one necessary action.
                2. Drink water or eat something simple if you can.
                3. Ask one real person for support if the situation is unsafe or too heavy.
                4. Return to /checkin when the state becomes clearer.
                """ : """
                Минимальный план

                1. Остановиться и сократить день до одного обязательного действия.
                2. Выпить воды или съесть что-то простое, если можешь.
                3. Попросить поддержки у реального человека, если ситуация небезопасная или слишком тяжёлая.
                4. Вернуться к /checkin, когда состояние станет понятнее.
                """;
        return new MinimalPlan(content.strip() + safety);
    }

    public record Input(TelegramUserEntity user, String text, RequestType requestType) implements Command {
    }
}
