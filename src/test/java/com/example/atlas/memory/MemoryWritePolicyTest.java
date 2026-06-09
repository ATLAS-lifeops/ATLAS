package com.example.atlas.memory;

import com.example.atlas.agent.AgentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryWritePolicyTest {

    private final MemoryWritePolicy policy = new MemoryWritePolicy();

    @Test
    void acceptsUsefulUserScopedPreference() {
        MemoryWrite write = write("User prefers one minimal plan for overloaded days.");

        assertThat(policy.validate(write).valid()).isTrue();
    }

    @Test
    void rejectsSecretsUnsafeMedicalClaimsAndMissingUserScope() {
        assertThat(policy.validate(write("api key: secret-value")).valid()).isFalse();
        assertThat(policy.validate(write("Prescribe a treatment plan for blood pressure medicine.")).valid()).isFalse();
        assertThat(policy.validate(new MemoryWrite(null, AgentType.PLANNER, MemoryType.PREFERENCE, MemoryScope.LONG_TERM, "x", "Useful preference", MemoryConfidence.HIGH, List.of(), MemorySource.PLANNER_AGENT, null)).valid()).isFalse();
    }

    private MemoryWrite write(String content) {
        return new MemoryWrite(
                UUID.randomUUID(),
                AgentType.PLANNER,
                MemoryType.PREFERENCE,
                MemoryScope.LONG_TERM,
                "Preference",
                content,
                MemoryConfidence.HIGH,
                List.of(new MemoryTag("planning")),
                MemorySource.PLANNER_AGENT,
                null
        );
    }
}
