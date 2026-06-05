package com.example.atlas.planning.application;

import com.example.atlas.life.service.LifeDayPlanService;
import com.example.atlas.planning.domain.DayPlan;
import com.example.atlas.shared.application.Command;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class CreateDayPlanUseCase implements UseCase<CreateDayPlanUseCase.Input, DayPlan> {

    private final Supplier<LifeDayPlanService> dayPlanService;

    @Autowired
    public CreateDayPlanUseCase(ObjectProvider<LifeDayPlanService> dayPlanService) {
        this.dayPlanService = dayPlanService::getIfAvailable;
    }

    public CreateDayPlanUseCase(LifeDayPlanService dayPlanService) {
        this(() -> dayPlanService);
    }

    private CreateDayPlanUseCase(Supplier<LifeDayPlanService> dayPlanService) {
        this.dayPlanService = dayPlanService;
    }

    @Override
    public DayPlan execute(Input input) {
        return new DayPlan(requireDayPlanService().dayPlan(input.user()));
    }

    private LifeDayPlanService requireDayPlanService() {
        LifeDayPlanService service = dayPlanService.get();
        if (service == null) {
            throw new IllegalStateException("Day plan service is not available.");
        }
        return service;
    }

    public record Input(TelegramUserEntity user) implements Command {
    }
}
