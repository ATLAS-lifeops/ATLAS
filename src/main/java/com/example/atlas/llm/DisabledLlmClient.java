package com.example.atlas.llm;

public class DisabledLlmClient implements LlmClient {

    @Override
    public LlmResponse chat(LlmRequest request) {
        throw new LlmUnavailableException("LLM is disabled or incomplete.");
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.DISABLED;
    }
}
