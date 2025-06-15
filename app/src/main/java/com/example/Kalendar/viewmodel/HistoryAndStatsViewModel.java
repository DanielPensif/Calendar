package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.domain.LoadStatsUseCase;
import com.example.Kalendar.domain.LoadStatsUseCase.StatsBundle;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HistoryAndStatsViewModel extends ViewModel {
    private final LoadStatsUseCase useCase;

    private final MutableLiveData<Integer> daysParam = new MutableLiveData<>(7);
    private final MutableLiveData<Void> histTrigger = new MutableLiveData<>();

    public final LiveData<StatsBundle> stats;
    public final LiveData<List<HistoryItem>> history;

    @Inject
    public HistoryAndStatsViewModel(LoadStatsUseCase useCase) {
        this.useCase = useCase;

        stats = Transformations.switchMap(daysParam, useCase::load);
        history = Transformations.switchMap(histTrigger, v -> useCase.loadHistory());

        histTrigger.setValue(null);
    }

    public void setDays(int d) {
        daysParam.setValue(d);
    }

    public void reloadHistory() {
        histTrigger.setValue(null);
    }

    public void saveAward(long timestamp, String calendarName, int which, int userId) {
        useCase.saveAward(timestamp, calendarName, which, userId);
        reloadHistory();
    }
}