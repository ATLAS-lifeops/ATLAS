package com.example.atlas.routines;

import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReminderSchedulerService {

    private final Set<String> sentReminders = ConcurrentHashMap.newKeySet();

    public boolean shouldSend(RoutinePreferencesEntity preferences, LocalTime now) {
        if (preferences == null || !preferences.isEnabled()) {
            return false;
        }
        LocalTime quietStart = LocalTime.parse(preferences.getQuietHoursStart());
        LocalTime quietEnd = LocalTime.parse(preferences.getQuietHoursEnd());
        return !insideQuietHours(now, quietStart, quietEnd);
    }

    public boolean claimReminder(RoutinePreferencesEntity preferences, String reminderType, LocalDate date, LocalTime now) {
        if (!shouldSend(preferences, now)) {
            return false;
        }
        String key = preferences.getTelegramUser().getId() + ":" + reminderType + ":" + date;
        return sentReminders.add(key);
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
