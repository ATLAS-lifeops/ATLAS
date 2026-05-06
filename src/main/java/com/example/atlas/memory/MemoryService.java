package com.example.atlas.memory;

import org.springframework.stereotype.Service;

@Service
public class MemoryService {

    public void rememberMessage(Long userId, String message) {
        // Persistence will be wired in v0.3.0.
    }
}
