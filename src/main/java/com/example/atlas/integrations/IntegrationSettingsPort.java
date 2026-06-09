package com.example.atlas.integrations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntegrationSettingsPort {

    Optional<IntegrationSettings> find(UUID userId, IntegrationType type);

    List<IntegrationSettings> findAll(UUID userId);

    IntegrationSettings save(IntegrationSettings settings);
}
