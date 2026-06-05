package com.example.atlas.profile.application;

import com.example.atlas.life.entity.LifeProfileEntity;
import com.example.atlas.life.service.LifeProfileService;
import com.example.atlas.profile.domain.LifeProfile;
import com.example.atlas.shared.application.Query;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetLifeProfileUseCase implements UseCase<GetLifeProfileUseCase.Input, Optional<LifeProfile>> {

    private final ObjectProvider<LifeProfileService> profileService;

    public GetLifeProfileUseCase(ObjectProvider<LifeProfileService> profileService) {
        this.profileService = profileService;
    }

    @Override
    public Optional<LifeProfile> execute(Input input) {
        LifeProfileService service = profileService.getIfAvailable();
        if (service == null) {
            return Optional.empty();
        }
        return service.find(input.user()).map(this::map);
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
