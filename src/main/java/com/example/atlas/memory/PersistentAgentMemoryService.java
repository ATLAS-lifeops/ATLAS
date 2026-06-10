package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;
import com.example.atlas.memory.entity.AgentMemoryRecordEntity;
import com.example.atlas.memory.repository.AgentMemoryRecordRepository;
import com.example.atlas.user.entity.TelegramUserEntity;
import com.example.atlas.user.repository.TelegramUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnBean({AgentMemoryRecordRepository.class, TelegramUserRepository.class})
public class PersistentAgentMemoryService implements AgentMemoryService {

    private final AgentMemoryRecordRepository repository;
    private final TelegramUserRepository userRepository;
    private final MemoryWritePolicy policy;
    private final MemorySnapshotWriter snapshotWriter;

    public PersistentAgentMemoryService(
            AgentMemoryRecordRepository repository,
            TelegramUserRepository userRepository,
            MemorySnapshotWriter snapshotWriter
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.snapshotWriter = snapshotWriter;
        this.policy = new MemoryWritePolicy();
    }

    @Override
    @Transactional
    public MemoryWriteResult write(MemoryWrite write) {
        MemoryValidationResult validation = policy.validate(write);
        if (!validation.valid()) {
            return MemoryWriteResult.rejected(validation.reason());
        }
        TelegramUserEntity user = userRepository.findById(write.userId()).orElse(null);
        if (user == null) {
            return MemoryWriteResult.rejected("unknown_user");
        }
        String deduplicationKey = policy.deduplicationKey(write);
        if (repository.findByInternalUserIdAndDeduplicationKeyAndArchivedFalse(user.getId(), deduplicationKey).isPresent()) {
            return MemoryWriteResult.rejected("duplicate");
        }
        Instant now = Instant.now();
        AgentMemoryRecordEntity entity = new AgentMemoryRecordEntity(
                UUID.randomUUID(),
                user,
                write.ownerAgent(),
                write.type(),
                write.scope(),
                write.title(),
                write.content(),
                write.confidence(),
                tags(write.tags()),
                write.source(),
                deduplicationKey,
                now,
                now,
                write.expiresAt(),
                false
        );
        repository.save(entity);
        snapshotWriter.writeSnapshots(user.getId(), findRecent(user.getId(), 50));
        return MemoryWriteResult.stored(entity.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentMemoryRecord> findForAgent(UUID userId, AgentType agentType, int limit) {
        return repository.findByInternalUserIdAndAgentTypeAndArchivedFalseOrderByUpdatedAtDesc(userId, agentType, PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentMemoryRecord> findSharedContext(UUID userId, int limit) {
        return repository.findByInternalUserIdAndMemoryScopeAndArchivedFalseOrderByUpdatedAtDesc(userId, MemoryScope.SHARED_CONTEXT, PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    @Transactional
    public void archiveForUser(UUID userId) {
        Instant now = Instant.now();
        repository.findByInternalUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId, PageRequest.of(0, 1000))
                .forEach(record -> record.archive(now));
    }

    @Transactional(readOnly = true)
    public long countActive(UUID userId) {
        return repository.countByInternalUserIdAndArchivedFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<AgentMemoryRecord> findRecent(UUID userId, int limit) {
        return repository.findByInternalUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId, PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(this::toRecord)
                .toList();
    }

    private AgentMemoryRecord toRecord(AgentMemoryRecordEntity entity) {
        return new AgentMemoryRecord(
                entity.getId(),
                entity.getInternalUserId(),
                entity.getAgentType(),
                entity.getMemoryType(),
                entity.getMemoryScope(),
                entity.getTitle(),
                entity.getContent(),
                entity.getConfidence(),
                parseTags(entity.getTags()),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getExpiresAt(),
                entity.isArchived()
        );
    }

    private String tags(List<MemoryTag> tags) {
        return tags.stream().map(MemoryTag::value).distinct().sorted().reduce((left, right) -> left + "," + right).orElse("");
    }

    private List<MemoryTag> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(",")).map(MemoryTag::new).toList();
    }
}
