package com.example.atlas.runtime.repository;

import com.example.atlas.runtime.entity.AtlasRuntimeSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AtlasRuntimeSettingsRepository extends JpaRepository<AtlasRuntimeSettingsEntity, UUID> {

    Optional<AtlasRuntimeSettingsEntity> findFirstByOrderByCreatedAtAsc();
}
