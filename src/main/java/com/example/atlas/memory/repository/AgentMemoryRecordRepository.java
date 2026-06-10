package com.example.atlas.memory.repository;

import com.example.atlas.agent.AgentType;
import com.example.atlas.memory.MemoryScope;
import com.example.atlas.memory.entity.AgentMemoryRecordEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentMemoryRecordRepository extends JpaRepository<AgentMemoryRecordEntity, UUID> {

    Optional<AgentMemoryRecordEntity> findByInternalUserIdAndDeduplicationKeyAndArchivedFalse(UUID internalUserId, String deduplicationKey);

    List<AgentMemoryRecordEntity> findByInternalUserIdAndAgentTypeAndArchivedFalseOrderByUpdatedAtDesc(UUID internalUserId, AgentType agentType, Pageable pageable);

    List<AgentMemoryRecordEntity> findByInternalUserIdAndMemoryScopeAndArchivedFalseOrderByUpdatedAtDesc(UUID internalUserId, MemoryScope memoryScope, Pageable pageable);

    List<AgentMemoryRecordEntity> findByInternalUserIdAndArchivedFalseOrderByUpdatedAtDesc(UUID internalUserId, Pageable pageable);

    long countByInternalUserIdAndArchivedFalse(UUID internalUserId);
}
