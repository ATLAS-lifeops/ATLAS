package com.example.atlas.llm;

public class LlmTimeoutException extends LlmClientException {

    public LlmTimeoutException(String message) {
        super(message);
    }

    public LlmTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
