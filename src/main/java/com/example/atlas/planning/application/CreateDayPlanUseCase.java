package com.example.atlas.planning.application;

import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.planning.domain.DayPlan;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(LifeDayPlanService.class)
public class CreateDayPlanUseCase implements UseCase<CreateDayPlanUseCase.Input, DayPlan> {

    private final LifeDayPlanService dayPlanService;

    public CreateDayPlanUseCase(LifeDayPlanService dayPlanService) {
        this.dayPlanService = dayPlanService;
    }

    @Override
    public DayPlan execute(Input input) {
        return new DayPlan(dayPlanService.dayPlan(input.user()));
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
