package com.example.Kalendar.utils;

import android.content.Context;

import androidx.room.Room;

import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.db.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper {

    private static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "kalendar_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    public static List<HistoryItem> getCompletedDays(Context context) {
        List<HistoryItem> result = new ArrayList<>();
        List<DayEntity> days = getDatabase(context).dayDao().getAllDays();

        for (DayEntity day : days) {
            List<TaskEntity> tasks = getDatabase(context).taskDao().getTasksByDayId(day.id);

            if (!tasks.isEmpty()) {
                boolean allCompleted = true;
                for (TaskEntity task : tasks) {
                    if (!task.done) {
                        allCompleted = false;
                        break;
                    }
                }

                if (allCompleted) {
                    String calendarName = getDatabase(context).calendarDao().getCalendarById(day.calendarId).title;
                    String formattedDate = new SimpleDateFormat("d MMMM", new Locale("ru")).format(day.timestamp);

                    result.add(new HistoryItem(formattedDate, calendarName, day.timestamp));
                }
            }
        }
        return result;
    }

    public static List<Integer> getTaskCountsForLast7Days(Context context, boolean onlyCompleted) {
        List<Integer> counts = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (int i = 6; i >= 0; i--) {
            Calendar dayCalendar = (Calendar) today.clone();
            dayCalendar.add(Calendar.DAY_OF_YEAR, -i);

            long dayStart = dayCalendar.getTimeInMillis();
            long dayEnd = dayStart + 86400000L; // +1 день в миллисекундах

            int taskCount = 0;

            List<DayEntity> days = getDatabase(context).dayDao().getDaysBetween(dayStart, dayEnd);
            for (DayEntity day : days) {
                List<TaskEntity> tasks = getDatabase(context).taskDao().getTasksByDayId(day.id);
                for (TaskEntity task : tasks) {
                    if (onlyCompleted) {
                        if (task.done) taskCount++;
                    } else {
                        taskCount++;
                    }
                }
            }

            counts.add(taskCount);
        }

        return counts;
    }
}
