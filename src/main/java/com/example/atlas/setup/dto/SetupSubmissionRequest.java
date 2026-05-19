package com.example.atlas.setup.dto;

public record SetupSubmissionRequest(
        String botToken,
        String botUsername,
        String mode,
        String publicBaseUrl,
        String webhookSecret
) {
}
