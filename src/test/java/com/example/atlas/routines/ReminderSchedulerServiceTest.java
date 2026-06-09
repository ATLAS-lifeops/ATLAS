package com.example.atlas.routines;

import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderSchedulerServiceTest {

    private final ReminderSchedulerService service = new ReminderSchedulerService();

    @Test
    void respectsQuietHoursAndEnabledFlag() {
        RoutinePreferencesEntity enabled = new RoutinePreferencesEntity(null, null, "09:00", "21:00", "Europe/Moscow", "22:00", "08:00", true, Instant.now());
        RoutinePreferencesEntity disabled = new RoutinePreferencesEntity(null, null, "09:00", "21:00", "Europe/Moscow", "22:00", "08:00", false, Instant.now());

        assertThat(service.shouldSend(enabled, LocalTime.parse("09:00"))).isTrue();
        assertThat(service.shouldSend(enabled, LocalTime.parse("23:00"))).isFalse();
        assertThat(service.shouldSend(disabled, LocalTime.parse("09:00"))).isFalse();
    }
}
