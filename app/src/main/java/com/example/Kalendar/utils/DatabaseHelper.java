package com.example.Kalendar.utils;

import android.content.Context;
import android.util.Pair;

import androidx.room.Room;

import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayAwardEntity;
import com.example.Kalendar.models.DayEntity;

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
        List<DayEntity> days = getDatabase(context).dayDao().getAll(); // Или фильтрация по выполненным, если появится статус
        List<HistoryItem> items = new ArrayList<>();
        for (DayEntity day : days) {
            String dateFormatted = android.text.format.DateFormat.format("d MMM", day.timestamp).toString();
            String calendarName = getDatabase(context).calendarDao().getCalendarById(day.calendarId).title;
            items.add(new HistoryItem(dateFormatted, calendarName, day.timestamp, day.calendarId));
        }
        return items;
    }

    public static void saveAwardForDay(Context context, int dayId, String awardType) {
        DayAwardEntity award = new DayAwardEntity();
        award.dayId = dayId;
        award.awardType = awardType;
        getDatabase(context).dayAwardDao().insertAward(award);
    }

    public static int getDayIdByTimestampAndCalendarId(Context context, long timestamp, int calendarId) {
        DayEntity day = getDatabase(context).dayDao().getByTimestampAndCalendarId(timestamp, calendarId);
        return day != null ? day.id : -1;
    }

    public static Map<Pair<Long, Integer>, String> getAwardsForCompletedDays(Context context) {
        List<DayAwardEntity> awards = getDatabase(context).dayAwardDao().getAll();
        Map<Pair<Long, Integer>, String> result = new HashMap<>();

        for (DayAwardEntity award : awards) {
            DayEntity day = getDatabase(context).dayDao().getById(award.dayId);
            if (day != null) {
                Pair<Long, Integer> key = new Pair<>(day.timestamp, day.calendarId);
                result.put(key, award.awardType);
            }
        }
        return result;
    }


    public static List<Integer> getTaskCountsForLastNDays(Context context, int n, boolean completedOnly) {
        Calendar calendar = Calendar.getInstance();
        long end = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, -n + 1);
        long start = calendar.getTimeInMillis();

        List<DayEntity> allDays = getDatabase(context).dayDao().getDaysBetween(start, end);
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
}
