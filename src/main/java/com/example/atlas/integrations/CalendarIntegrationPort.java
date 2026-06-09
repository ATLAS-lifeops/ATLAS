package com.example.atlas.integrations;

import java.util.List;
import java.util.UUID;

public interface CalendarIntegrationPort {

    List<CalendarEventDraft> previewWeeklyPlan(UUID userId);
}
