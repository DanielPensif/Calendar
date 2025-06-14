package com.example.Kalendar.viewmodel;

import org.threeten.bp.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarContent {
    public final List<LocalDate> daysInMonth;
    public final Map<LocalDate, Set<Integer>> activeDayCalendars;
    public final Map<Integer, String> colorMap;
    public final Map<LocalDate, String> awardsMap;
    public final List<com.example.Kalendar.models.CalendarEntity> allCalendars;
    public final int currentCalendarId;
    public final int streak;

    public CalendarContent(
            List<LocalDate> daysInMonth,
            Map<LocalDate, Set<Integer>> activeDayCalendars,
            Map<Integer, String> colorMap,
            Map<LocalDate, String> awardsMap,
            List<com.example.Kalendar.models.CalendarEntity> allCalendars,
            int currentCalendarId,
            int streak
    ) {
        this.daysInMonth = daysInMonth;
        this.activeDayCalendars = activeDayCalendars;
        this.colorMap = colorMap;
        this.awardsMap = awardsMap;
        this.allCalendars = allCalendars;
        this.currentCalendarId = currentCalendarId;
        this.streak = streak;
    }
}