package com.example.atlas.llm;

public interface LlmClient {

    LlmResponse chat(LlmRequest request);

    boolean available();

    LlmProvider provider();
}
