package com.example.atlas.integrations;

import com.example.atlas.integrations.entity.IntegrationSettingsEntity;
import com.example.atlas.integrations.repository.IntegrationSettingsRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.repository.TelegramUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean({IntegrationSettingsRepository.class, TelegramUserRepository.class})
public class PersistentIntegrationSettingsAdapter implements IntegrationSettingsPort {

    private final IntegrationSettingsRepository repository;
    private final TelegramUserRepository userRepository;

    public PersistentIntegrationSettingsAdapter(IntegrationSettingsRepository repository, TelegramUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IntegrationSettings> find(UUID userId, IntegrationType type) {
        TelegramUserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        return repository.findByTelegramUserAndIntegrationType(user, type).map(this::toSettings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationSettings> findAll(UUID userId) {
        TelegramUserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        return repository.findByTelegramUserOrderByIntegrationTypeAsc(user).stream()
                .map(this::toSettings)
                .toList();
    }

    @Override
    @Transactional
    public IntegrationSettings save(IntegrationSettings settings) {
        TelegramUserEntity user = userRepository.findById(settings.userId()).orElseThrow();
        Instant now = Instant.now();
        IntegrationSettingsEntity entity = repository.findByTelegramUserAndIntegrationType(user, settings.type())
                .map(existing -> {
                    existing.update(settings.status(), encode(settings.safeMetadata()), now);
                    return existing;
                })
                .orElseGet(() -> new IntegrationSettingsEntity(UUID.randomUUID(), user, settings.type(), settings.status(), encode(settings.safeMetadata()), now));
        return toSettings(repository.save(entity));
    }

    private IntegrationSettings toSettings(IntegrationSettingsEntity entity) {
        return new IntegrationSettings(
                entity.getTelegramUser().getId(),
                entity.getIntegrationType(),
                entity.getStatus(),
                decode(entity.getSafeMetadataJson())
        );
    }

    private String encode(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        return metadata.entrySet().stream()
                .map(entry -> safe(entry.getKey()) + "=" + safe(entry.getValue()))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private Map<String, String> decode(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(metadata.split("\n"))
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toUnmodifiableMap(parts -> parts[0], parts -> parts[1], (left, right) -> right));
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\n", " ").replace("\r", " ").replace("=", ":").strip();
    }
}
