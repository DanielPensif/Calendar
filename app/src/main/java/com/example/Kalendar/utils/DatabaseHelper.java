package com.example.Kalendar.utils;

import android.content.Context;
import android.util.Pair;

import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper {

    public static AppDatabase getDatabase(Context context) {
        return AppDatabase.getDatabase(context);
    }

    public static List<HistoryItem> getCompletedDays(Context context) {
        List<DayEntity> days = getDatabase(context).dayDao().getAll();
        List<HistoryItem> items = new ArrayList<>();

        for (DayEntity day : days) {
            // Проверка: день должен иметь хотя бы одну выполненную задачу
            int completedCount = getDatabase(context).taskDao().getCompletedTaskCountForDay(day.id);
            if (completedCount > 0) {
                String dateFormatted = android.text.format.DateFormat
                        .format("d MMM", day.timestamp).toString();
                String calendarName = getDatabase(context)
                        .calendarDao().getCalendarById(day.calendarId).title;
                items.add(new HistoryItem(dateFormatted, calendarName, day.timestamp, day.calendarId));
            }
        }
        return items;
    }

    public static void saveAwardForDay(Context context, int dayId, String awardType) {
        AppDatabase db = getDatabase(context);
        DayEntity day = db.dayDao().getById(dayId);
        if (day != null) {
            day.awardType = awardType;
            db.dayDao().update(day);
        }
    }

    public static int getDayIdByTimestampAndCalendarId(Context context, long timestamp, int calendarId) {
        DayEntity day = getDatabase(context)
                .dayDao().getByTimestampAndCalendarId(timestamp, calendarId);
        return day != null ? day.id : -1;
    }

    public static Map<Pair<Long, Integer>, String> getAwardsForCompletedDays(Context context) {
        List<DayEntity> days = getDatabase(context).dayDao().getAll();
        Map<Pair<Long, Integer>, String> result = new HashMap<>();
        for (DayEntity day : days) {
            if (day.awardType != null) {
                int totalTasks = getDatabase(context).taskDao().getTotalTaskCountByDayId(day.id);
                int doneTasks = getDatabase(context).taskDao().getCompletedTaskCountByDayId(day.id);
                if (totalTasks > 0 && totalTasks == doneTasks) {
                    Pair<Long, Integer> key = new Pair<>(day.timestamp, day.calendarId);
                    result.put(key, day.awardType);
                }
            }
        }
        return result;
    }

    public static List<Integer> getTaskCountsForLastNDays(Context context, int n, boolean completedOnly) {
        Calendar calendar = Calendar.getInstance();
        long end = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        long start = calendar.getTimeInMillis();

        List<DayEntity> allDays = getDatabase(context)
                .dayDao().getDaysBetween(start, end);
        List<Integer> counts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            long dayStart = start + i * 86400000L;
            long dayEnd = dayStart + 86400000L;
            int count = 0;
            for (DayEntity day : allDays) {
                if (day.timestamp >= dayStart && day.timestamp < dayEnd) {
                    count++;
                }
            }
            counts.add(count);
        }
        return counts;
    }

    public static Pair<List<Integer>, List<Integer>> getCompletedAndUncompletedCounts(Context context, int n) {
        Calendar calendar = Calendar.getInstance();
        long end = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        long start = calendar.getTimeInMillis();

        List<DayEntity> allDays = getDatabase(context).dayDao().getDaysBetween(start, end);

        List<Integer> completedCounts = new ArrayList<>();
        List<Integer> uncompletedCounts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            long dayStart = start + i * 86400000L;
            long dayEnd = dayStart + 86400000L;

            int completedSum = 0;
            int uncompletedSum = 0;

            for (DayEntity day : allDays) {
                if (day.timestamp >= dayStart && day.timestamp < dayEnd) {
                    int total = getDatabase(context).taskDao().getTotalTaskCountByDayId(day.id);
                    int done = getDatabase(context).taskDao().getCompletedTaskCountByDayId(day.id);
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
        Calendar calendar = Calendar.getInstance();
        // Устанавливаем время конца как конец сегодняшнего дня (опционально)
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long end = calendar.getTimeInMillis();

        // Начало — ровно n-1 дней назад в 00:00
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long start = calendar.getTimeInMillis();

        // Загружаем все дни в этом интервале
        List<DayEntity> allDays = getDatabase(context)
                .dayDao().getDaysBetween(start, end);

        List<int[]> stats = new ArrayList<>(); // для каждого дня — [allTasks, completedTasks, notCompletedTasks]

        for (int i = 0; i < n; i++) {
            // границы i-го дня
            long dayStart = start + i * 86_400_000L;
            long dayEnd = dayStart + 86_400_000L - 1;

            int allTasks = 0;
            int completedTasks = 0;

            // для каждого DayEntity, попавшего в интервал
            for (DayEntity day : allDays) {
                if (day.timestamp >= dayStart && day.timestamp <= dayEnd) {
                    // получаем все задачи по dayId
                    List<TaskEntity> tasks = getDatabase(context)
                            .taskDao().getTasksByDayId(day.id);

                    allTasks += tasks.size();
                    // считаем выполненные
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
            AppDatabase db = AppDatabase.getDatabase(context);
            DayEntity day = db.dayDao().getById(dayId);
            if (day == null) return;

            // Получаем все задачи для этого дня
            List<TaskEntity> tasks = db.taskDao().getTasksByDayId(dayId);

            // Проверяем, есть ли задачи и все ли они выполнены
            boolean allDone = !tasks.isEmpty();
            for (TaskEntity t : tasks) {
                if (!t.done) {
                    allDone = false;
                    break;
                }
            }
            // Если день не полностью выполнен — сбросить награду
            if (!allDone && day.awardType != null) {
                day.awardType = null;
                db.dayDao().update(day);
            }

        }).start();
    }

}
