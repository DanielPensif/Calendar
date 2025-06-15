package com.example.Kalendar.repository;

import android.content.Context;

import com.example.Kalendar.utils.DatabaseHelper;

import java.util.List;
import java.util.Map;

import androidx.core.util.Pair;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StatisticsRepository {
    private final Context ctx;

    @Inject
    public StatisticsRepository(@ApplicationContext Context ctx) {
        this.ctx = ctx;
    }

    public List<Integer> getTotalTasks(int days, boolean completed) {
        return DatabaseHelper.getTaskCountsForLastNDays(ctx, days, completed);
    }

    public List<int[]> getDetailedStats(int days) {
        return DatabaseHelper.getDetailedTaskStatsForLastNDays(ctx, days);
    }

    public List<com.example.Kalendar.adapters.HistoryItem> getHistory() {
        return DatabaseHelper.getCompletedDays(ctx);
    }

    public Map<Pair<Long, Integer>, String> getAwards() {
        return DatabaseHelper.getAwardsForCompletedDays(ctx);
    }

    public void saveAward(long timestamp, String calendarName, int which, int userId) {
        int calId = DatabaseHelper.getDatabase(ctx).calendarDao().getIdByName(calendarName, userId);
        int dayId = DatabaseHelper.getDayIdByTimestampAndCalendarId(ctx, timestamp, calId);
        String code = which==0?"cup": which==1?"medal":"gold_border";
        if (dayId != -1) DatabaseHelper.saveAwardForDay(ctx, dayId, code);
    }
}