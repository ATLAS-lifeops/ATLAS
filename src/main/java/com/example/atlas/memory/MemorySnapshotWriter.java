package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;
import com.example.atlas.config.AtlasProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class MemorySnapshotWriter {

    private final AtlasProperties properties;

    public MemorySnapshotWriter(AtlasProperties properties) {
        this.properties = properties;
    }

    public void writeSnapshots(UUID userId, List<AgentMemoryRecord> records) {
        if (!properties.memory().snapshotsEnabled()) {
            return;
        }
        Path root = Path.of(properties.memory().snapshotPath(), "users", userId.toString());
        try {
            Files.createDirectories(root.resolve("agents"));
            Files.writeString(root.resolve("shared-context.md"), render(records.stream()
                    .filter(record -> record.scope() == MemoryScope.SHARED_CONTEXT)
                    .toList()));
            for (AgentType agentType : AgentType.values()) {
                Files.writeString(root.resolve("agents").resolve(agentType.name().toLowerCase() + ".md"), render(records.stream()
                        .filter(record -> record.agentType() == agentType)
                        .toList()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write memory snapshot", exception);
        }
    }

    private String render(List<AgentMemoryRecord> records) {
        if (records.isEmpty()) {
            return "# ATLAS memory\n\nNo records.\n";
        }
        StringBuilder builder = new StringBuilder("# ATLAS memory\n\n");
        records.stream()
                .sorted(Comparator.comparing(AgentMemoryRecord::updatedAt).reversed())
                .forEach(record -> builder
                        .append("## ").append(safe(record.title())).append("\n\n")
                        .append("- Type: ").append(record.type()).append("\n")
                        .append("- Scope: ").append(record.scope()).append("\n")
                        .append("- Confidence: ").append(record.confidence()).append("\n\n")
                        .append(safe(record.content())).append("\n\n"));
        return builder.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value.replaceAll("(?i)(token|api key|secret|password)\\s*[:=]\\s*\\S+", "$1=");
    }
}
