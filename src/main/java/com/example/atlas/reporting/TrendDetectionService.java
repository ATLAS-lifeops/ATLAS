package com.example.atlas.reporting;

import com.example.atlas.checkin.entity.CheckInEntity;
import com.example.atlas.habit.entity.HabitCheckEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrendDetectionService {

    public TrendSummary summarize(List<CheckInEntity> checkIns, List<HabitCheckEntity> habits) {
        return new TrendSummary(
                trend(checkIns.stream().map(CheckInEntity::getEnergy).toList()),
                trend(checkIns.stream().map(CheckInEntity::getFocus).toList()),
                trend(checkIns.stream().map(CheckInEntity::getStress).toList()),
                trend(checkIns.stream().map(CheckInEntity::getSleepQuality).toList()),
                habitConsistency(habits)
        );
    }

    private String trend(List<Integer> values) {
        List<Integer> present = values.stream().filter(value -> value != null).toList();
        if (present.size() < 2) {
            return "not_enough_data";
        }
        int first = present.getLast();
        int last = present.getFirst();
        if (last >= first + 2) {
            return "up";
        }
        if (last <= first - 2) {
            return "down";
        }
        return "stable";
    }

    private String habitConsistency(List<HabitCheckEntity> habits) {
        if (habits.isEmpty()) {
            return "not_enough_data";
        }
        long completed = habits.stream().filter(HabitCheckEntity::isCompleted).count();
        double ratio = (double) completed / habits.size();
        if (ratio >= 0.8) {
            return "high";
        }
        if (ratio >= 0.4) {
            return "mixed";
        }
        return "low";
    }
}
