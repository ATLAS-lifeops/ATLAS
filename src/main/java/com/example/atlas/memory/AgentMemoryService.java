package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;

import java.util.List;
import java.util.UUID;

public interface AgentMemoryService {

    MemoryWriteResult write(MemoryWrite write);

    List<AgentMemoryRecord> findForAgent(UUID userId, AgentType agentType, int limit);

    List<AgentMemoryRecord> findSharedContext(UUID userId, int limit);

    void archiveForUser(UUID userId);
}
