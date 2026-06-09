package com.example.atlas.routines;

import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class ReminderSchedulerService {

    public boolean shouldSend(RoutinePreferencesEntity preferences, LocalTime now) {
        if (preferences == null || !preferences.isEnabled()) {
            return false;
        }
        LocalTime quietStart = LocalTime.parse(preferences.getQuietHoursStart());
        LocalTime quietEnd = LocalTime.parse(preferences.getQuietHoursEnd());
        return !insideQuietHours(now, quietStart, quietEnd);
    }

    private boolean insideQuietHours(LocalTime now, LocalTime start, LocalTime end) {
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }
}
