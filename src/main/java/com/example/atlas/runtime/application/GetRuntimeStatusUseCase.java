package com.example.atlas.runtime.application;

import com.example.atlas.config.AtlasProperties;
import com.example.atlas.runtime.domain.RuntimeStatus;
import com.example.atlas.shared.application.Query;
import com.example.atlas.shared.application.UseCase;
import org.springframework.stereotype.Service;

@Service
public class GetRuntimeStatusUseCase implements UseCase<GetRuntimeStatusUseCase.Input, RuntimeStatus> {

    private final AtlasProperties properties;

    public GetRuntimeStatusUseCase(AtlasProperties properties) {
        this.properties = properties;
    }

    @Override
    public RuntimeStatus execute(Input input) {
        return new RuntimeStatus("UP", properties.setup().enabled());
    }

    public record Input() implements Query {
    }
}
