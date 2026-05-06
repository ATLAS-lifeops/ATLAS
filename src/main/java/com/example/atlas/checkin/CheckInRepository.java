package com.example.atlas.checkin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByUserIdOrderByCreatedAtDesc(Long userId);
}
