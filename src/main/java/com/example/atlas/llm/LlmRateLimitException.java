package com.example.atlas.llm;

public class LlmRateLimitException extends LlmClientException {

    public LlmRateLimitException(String message) {
        super(message);
    }
}
