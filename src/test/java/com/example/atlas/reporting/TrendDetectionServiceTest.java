package com.example.atlas.reporting;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.habit.entity.HabitCheckEntity;
import com.example.atlas.user.entity.TelegramUserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrendDetectionServiceTest {

    @Test
    void detectsDeterministicTrendsAndHabitConsistency() {
        TelegramUserEntity user = TelegramUserEntity.create(7L, 42L, "user", "User", Instant.now());
        CheckInEntity oldCheckIn = CheckInEntity.create(user, 3, null, 4, 5, 8, 5, "", false, false, "", Instant.parse("2026-06-01T08:00:00Z"));
        CheckInEntity latestCheckIn = CheckInEntity.create(user, 7, null, 4, 4, 5, 5, "", false, false, "", Instant.parse("2026-06-02T08:00:00Z"));
        HabitCheckEntity habit = HabitCheckEntity.create(user, "Read", "2 pages", true, "", Instant.now());

        TrendSummary summary = new TrendDetectionService().summarize(List.of(latestCheckIn, oldCheckIn), List.of(habit));

        assertThat(summary.energyTrend()).isEqualTo("up");
        assertThat(summary.focusTrend()).isEqualTo("stable");
        assertThat(summary.stressTrend()).isEqualTo("down");
        assertThat(summary.habitConsistency()).isEqualTo("high");
    }
}
