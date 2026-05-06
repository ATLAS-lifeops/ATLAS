package com.example.atlas.checkin;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CheckInService {

    private final CheckInRepository repository;

    public CheckInService(CheckInRepository repository) {
        this.repository = repository;
    }

    public CheckIn save(Long userId, Integer energy, Integer fatigue, String notes) {
        return repository.save(new CheckIn(userId, energy, fatigue, notes, Instant.now()));
    }
}
