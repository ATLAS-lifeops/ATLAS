package com.example.atlas.tracking.application;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.checkin.repository.CheckInRepository;
import com.example.atlas.shared.application.Query;
import com.example.atlas.shared.application.UseCase;
import com.example.atlas.tracking.domain.CheckIn;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetLatestCheckInUseCase implements UseCase<GetLatestCheckInUseCase.Input, Optional<CheckIn>> {

    private final ObjectProvider<CheckInRepository> repository;

    public GetLatestCheckInUseCase(ObjectProvider<CheckInRepository> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CheckIn> execute(Input input) {
        CheckInRepository checkIns = repository.getIfAvailable();
        if (checkIns == null) {
            return Optional.empty();
        }
        return checkIns.findByTelegramUserOrderByCreatedAtDesc(input.user()).stream()
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
