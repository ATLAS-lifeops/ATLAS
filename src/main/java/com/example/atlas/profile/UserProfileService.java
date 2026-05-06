package com.example.atlas.profile;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public Optional<UserProfile> findByUserId(Long userId) {
        return repository.findById(userId);
    }
}
