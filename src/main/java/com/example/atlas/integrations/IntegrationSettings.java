package com.example.atlas.integrations;

import java.util.Map;
import java.util.UUID;

public record IntegrationSettings(
        UUID userId,
        IntegrationType type,
        IntegrationStatus status,
        Map<String, String> safeMetadata
) {
    public IntegrationSettings {
        safeMetadata = safeMetadata == null ? Map.of() : Map.copyOf(safeMetadata);
    }
}
