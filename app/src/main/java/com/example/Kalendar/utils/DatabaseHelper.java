package com.example.Kalendar.utils;

import android.content.Context;

import androidx.room.Room;

import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper {

    private static AppDatabase getDatabase(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "calendar_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    // Получаем все выполненные дни для истории
    public static List<HistoryItem> getCompletedDays(Context context) {
        List<HistoryItem> result = new ArrayList<>();
        List<DayEntity> allDays = getDatabase(context).dayDao().getAllDays();

        for (DayEntity day : allDays) {
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
    public static List<Integer> getTaskCountsForLastNDays(Context context, int days, boolean onlyCompleted) {
        List<Integer> counts = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        AppDatabase db = getDatabase(context);
        List<DayEntity> allDays = db.dayDao().getAllDays();

        for (int i = days - 1; i >= 0; i--) {
            Calendar targetDay = (Calendar) today.clone();
            targetDay.add(Calendar.DAY_OF_YEAR, -i);

            long dayStart = targetDay.getTimeInMillis();
            long dayEnd = dayStart + 86400000L - 1;

            int count = 0;

            for (DayEntity day : allDays) {
                if (day.timestamp >= dayStart && day.timestamp <= dayEnd) {
                    List<TaskEntity> tasks = db.taskDao().getTasksByDayId(day.id);
                    for (TaskEntity task : tasks) {
                        if (onlyCompleted) {
                            if (task.done) count++;
                        } else {
                            count++;
                        }
                    }
                }
            }

            counts.add(count);
        }

        return counts;
    }
}
