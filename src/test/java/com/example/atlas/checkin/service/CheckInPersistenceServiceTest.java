package com.example.atlas.checkin.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class CheckInPersistenceServiceTest {

    private final CheckInPersistenceService service = new CheckInPersistenceService(null, Clock.systemUTC());

    @Test
    void parseExtractsMetricsAndPainFlag() {
        CheckInPersistenceService.ParsedCheckIn parsed = service.parse(
                "/checkin energy 7 fatigue 4 sleep 8 stress 3 knee pain after workout"
        );

        assertThat(parsed.energy()).isEqualTo(7);
        assertThat(parsed.fatigue()).isEqualTo(4);
        assertThat(parsed.sleepQuality()).isEqualTo(8);
        assertThat(parsed.stress()).isEqualTo(3);
        assertThat(parsed.painFlag()).isTrue();
    }
}
