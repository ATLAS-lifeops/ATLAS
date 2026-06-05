package com.example.atlas.shared.errors;

public class AtlasException extends RuntimeException {

    public AtlasException(String message) {
        super(message);
    }

    public AtlasException(String message, Throwable cause) {
        super(message, cause);
    }
}
