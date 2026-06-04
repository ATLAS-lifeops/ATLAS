package com.example.atlas.llm;

public class LlmUnavailableException extends LlmClientException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
