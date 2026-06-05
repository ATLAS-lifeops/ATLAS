package com.example.atlas.profile.application;

import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.profile.domain.LifeProfile;
import com.example.atlas.shared.application.Query;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnBean(LifeProfileService.class)
public class GetLifeProfileUseCase implements UseCase<GetLifeProfileUseCase.Input, Optional<LifeProfile>> {

    private final LifeProfileService profileService;

    public GetLifeProfileUseCase(LifeProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Optional<LifeProfile> execute(Input input) {
        return profileService.find(input.user()).map(this::map);
    }

    private LifeProfile map(LifeProfileEntity entity) {
        return new LifeProfile(
                entity.getPrimaryLifeArea() == null ? null : entity.getPrimaryLifeArea().name(),
                entity.getCurrentFocus(),
                entity.getPlanningStyle() == null ? null : entity.getPlanningStyle().name(),
                entity.isOnboardingCompleted()
        );
    }

    public record Input(TelegramUserEntity user) implements Query {
    }
}
