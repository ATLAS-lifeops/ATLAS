package com.example.atlas.agent;

public enum AgentFailureReason {
    NONE,
    LLM_DISABLED,
    LLM_UNAVAILABLE,
    OUT_OF_SCOPE,
    SAFETY_BLOCKED
}
