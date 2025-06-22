package com.example.Kalendar.utils;

import android.content.Context;
import androidx.core.util.Pair;

import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper {

    public static AppDatabase getDatabase(Context context) {
        return AppDatabase.getDatabase(context);
    }

    public static List<HistoryItem> getCompletedDays(Context context) {
        AppDatabase db = getDatabase(context);

        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return Collections.emptyList();

        List<CalendarEntity> cals = db.calendarDao().getByUserId(userId);
        if (cals.isEmpty()) return Collections.emptyList();

        List<Integer> calIds = new ArrayList<>(cals.size());
        Map<Integer, String> calTitles = new HashMap<>();
        for (CalendarEntity c : cals) {
            calIds.add(c.getId());
            calTitles.put(c.getId(), c.getTitle());
        }

        List<DayEntity> days = db.dayDao().getByCalendarIds(calIds);

        List<HistoryItem> items = new ArrayList<>();
        for (DayEntity day : days) {
            int total = db.taskDao().getTotalTaskCountByDayId(day.getId());
            int done = db.taskDao().getCompletedTaskCountByDayId(day.getId());

            if (total > 0 && total == done) {
                String dateFormatted = android.text.format.DateFormat
                        .format("d MMM", day.getTimestamp()).toString();
                String calName = calTitles.get(day.getCalendarId());
                items.add(new HistoryItem(dateFormatted, calName, day.getTimestamp(), day.getCalendarId(), day.getAwardType()));
            }
        }
        return items;
    }

    public static void saveAwardForDay(Context context, int dayId, String awardType) {
        AppDatabase db = getDatabase(context);
        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return;

        DayEntity day = db.dayDao().getById(dayId);
        if (day != null) {
            CalendarEntity cal = db.calendarDao().getById(day.getCalendarId(), userId);
            if (cal != null && cal.getUserId() == userId) {
                day.setAwardType(awardType);
                db.dayDao().update(day);
            }
        }
    }

    public static int getDayIdByTimestampAndCalendarId(Context context, long timestamp, int calendarId) {
        DayEntity day = getDatabase(context)
                .dayDao().getByTimestampAndCalendarId(timestamp, calendarId);
        return day != null ? day.getId() : -1;
    }

    public static Map<Pair<Long, Integer>, String> getAwardsForCompletedDays(Context context) {
        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return Collections.emptyMap();

        AppDatabase db = getDatabase(context);

        List<CalendarEntity> userCalendars = db.calendarDao().getByUserId(userId);
        List<Integer> calIds = new ArrayList<>();
        for (CalendarEntity c : userCalendars) calIds.add(c.getId());

        List<DayEntity> days = db.dayDao().getByCalendarIds(calIds);

        Map<Pair<Long, Integer>, String> result = new HashMap<>();
        for (DayEntity day : days) {
            if (day.getAwardType() != null) {
                int totalTasks = db.taskDao().getTotalTaskCountByDayId(day.getId());
                int doneTasks = db.taskDao().getCompletedTaskCountByDayId(day.getId());
                if (totalTasks > 0 && totalTasks == doneTasks) {
                    Pair<Long, Integer> key = new Pair<>(day.getTimestamp(), day.getCalendarId());
                    result.put(key, day.getAwardType());
                }
            }
        }
        return result;
    }

    public static List<Integer> getTaskCountsForLastNDays(Context context, int n, boolean completedOnly) {
        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return Collections.emptyList();

        Calendar calendar = Calendar.getInstance();
        long end = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        long start = calendar.getTimeInMillis();

        AppDatabase db = getDatabase(context);
        List<CalendarEntity> userCalendars = db.calendarDao().getByUserId(userId);
        List<Integer> calIds = new ArrayList<>();
        for (CalendarEntity c : userCalendars) calIds.add(c.getId());

        List<DayEntity> allDays = db.dayDao().getDaysBetweenForCalendars(start, end, calIds);
        List<Integer> counts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            long dayStart = start + i * 86400000L;
            long dayEnd = dayStart + 86400000L;
            int count = 0;
            for (DayEntity day : allDays) {
                if (day.getTimestamp() >= dayStart && day.getTimestamp() < dayEnd) {
                    count++;
                }
            }
            counts.add(count);
        }
        return counts;
    }

    public static Pair<List<Integer>, List<Integer>> getCompletedAndUncompletedCounts(Context context, int n) {
        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return new Pair<>(Collections.emptyList(), Collections.emptyList());

        Calendar calendar = Calendar.getInstance();
        long end = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        long start = calendar.getTimeInMillis();

        AppDatabase db = getDatabase(context);
        List<CalendarEntity> userCalendars = db.calendarDao().getByUserId(userId);
        List<Integer> calIds = new ArrayList<>();
        for (CalendarEntity c : userCalendars) calIds.add(c.getId());

        List<DayEntity> allDays = db.dayDao().getDaysBetweenForCalendars(start, end, calIds);

        List<Integer> completedCounts = new ArrayList<>();
        List<Integer> uncompletedCounts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            long dayStart = start + i * 86400000L;
            long dayEnd = dayStart + 86400000L;

            int completedSum = 0;
            int uncompletedSum = 0;

            for (DayEntity day : allDays) {
                if (day.getTimestamp() >= dayStart && day.getTimestamp() < dayEnd) {
                    int total = db.taskDao().getTotalTaskCountByDayId(day.getId());
                    int done = db.taskDao().getCompletedTaskCountByDayId(day.getId());
                    completedSum += done;
                    uncompletedSum += (total - done);
                }
            }

            completedCounts.add(completedSum);
            uncompletedCounts.add(uncompletedSum);
        }

        return new Pair<>(completedCounts, uncompletedCounts);
    }

    public static List<int[]> getDetailedTaskStatsForLastNDays(Context context, int n) {
        int userId = SessionManager.getLoggedInUserId(context);
        if (userId == -1) return Collections.emptyList();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long end = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();

        AppDatabase db = getDatabase(context);
        List<CalendarEntity> userCalendars = db.calendarDao().getByUserId(userId);
        List<Integer> calIds = new ArrayList<>();
        for (CalendarEntity c : userCalendars) calIds.add(c.getId());

        List<DayEntity> allDays = db.dayDao().getDaysBetweenForCalendars(start, end, calIds);

        List<int[]> stats = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            long dayStart = start + i * 86400000L;
            long dayEnd = dayStart + 86400000L - 1;

            int allTasks = 0;
            int completedTasks = 0;

            for (DayEntity day : allDays) {
                if (day.getTimestamp() >= dayStart && day.getTimestamp() <= dayEnd) {
                    List<TaskEntity> tasks = db.taskDao().getTasksByDayId(day.getId());
                    allTasks += tasks.size();
                    for (TaskEntity t : tasks) {
                        if (t.done) completedTasks++;
                    }
                }
            }

            int notCompletedTasks = allTasks - completedTasks;
            stats.add(new int[]{ allTasks, completedTasks, notCompletedTasks });
        }

        return stats;
    }

    public static void updateDayCompletionStatus(Context context, int dayId) {
        new Thread(() -> {
            AppDatabase db = getDatabase(context);
            int userId = SessionManager.getLoggedInUserId(context);
            if (userId == -1) return;

            DayEntity day = db.dayDao().getById(dayId);
            if (day == null) return;

            CalendarEntity cal = db.calendarDao().getById(day.getCalendarId(), userId);
            if (cal == null || cal.getUserId() != userId) return;

            List<TaskEntity> tasks = db.taskDao().getTasksByDayId(dayId);

            boolean allDone = !tasks.isEmpty();
            for (TaskEntity t : tasks) {
                if (!t.done) {
                    allDone = false;
                    break;
                }
            }

            if (!allDone && day.getAwardType() != null) {
                day.setAwardType(null);
                db.dayDao().update(day);
            }

        }).start();
    }
}
