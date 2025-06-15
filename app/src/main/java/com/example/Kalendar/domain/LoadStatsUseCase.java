package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.repository.StatisticsRepository;

import java.util.List;
import java.util.Map;

import androidx.core.util.Pair;
import com.example.Kalendar.adapters.HistoryItem;

import javax.inject.Inject;
import dagger.hilt.android.scopes.ActivityRetainedScoped;

@ActivityRetainedScoped
public class LoadStatsUseCase {
    private final StatisticsRepository repo;

    @Inject
    public LoadStatsUseCase(StatisticsRepository repo) {
        this.repo = repo;
    }

    public LiveData<StatsBundle> load(int days) {
        MutableLiveData<StatsBundle> live = new MutableLiveData<>();
        new Thread(() -> {
            var total   = repo.getTotalTasks(days, false);
            var done    = repo.getTotalTasks(days, true);
            var detail  = repo.getDetailedStats(days);
            live.postValue(new StatsBundle(total, done, detail));
        }).start();
        return live;
    }

    public LiveData<List<HistoryItem>> loadHistory() {
        MutableLiveData<List<HistoryItem>> live = new MutableLiveData<>();
        new Thread(() -> {
            var items = repo.getHistory();
            var awards = repo.getAwards();
            for (var it : items) {
                var key = Pair.create(it.timestamp, it.calendarId);
                it.award = awards.get(key);
            }
            live.postValue(items);
        }).start();
        return live;
    }

    public void saveAward(long timestamp, String calName, int which, int userId) {
        new Thread(() -> repo.saveAward(timestamp, calName, which, userId)).start();
    }

    public static class StatsBundle {
        public final List<Integer> total, done;
        public final List<int[]> detail;
        public StatsBundle(List<Integer> t, List<Integer> d, List<int[]> dt) {
            total = t; done = d; detail = dt;
        }
    }
}