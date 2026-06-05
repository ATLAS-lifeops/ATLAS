package com.example.atlas.tracking.application;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.shared.application.Query;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.tracking.domain.CheckIn;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnBean(CheckInRepository.class)
public class GetLatestCheckInUseCase implements UseCase<GetLatestCheckInUseCase.Input, Optional<CheckIn>> {

    private final CheckInRepository repository;

    public GetLatestCheckInUseCase(CheckInRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CheckIn> execute(Input input) {
        return repository.findByTelegramUserOrderByCreatedAtDesc(input.user()).stream()
                .findFirst()
                .map(this::map);
    }

    private CheckIn map(CheckInEntity entity) {
        return new CheckIn(
                entity.getEnergy(),
                entity.getFocus(),
                entity.getStress(),
                entity.getSleepQuality(),
                entity.getMood(),
                entity.getMainPriority(),
                entity.getCreatedAt()
        );
    }

    public record Input(TelegramUserEntity user) implements Query {
    }
}
