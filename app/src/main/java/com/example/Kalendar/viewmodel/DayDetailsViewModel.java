package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.LoadDayContentUseCase;

import org.threeten.bp.LocalDate;

import java.util.List;

import javax.inject.Inject;

public class DayDetailsViewModel extends ViewModel {
    private LoadDayContentUseCase uc;
    private final MutableLiveData<Params> trigger = new MutableLiveData<>();

    public final LiveData<DayContent> content = Transformations.switchMap(trigger,
            p -> uc.execute(p.date, p.calIds)
    );

    @Inject
    public DayDetailsViewModel(LoadDayContentUseCase uc) {
        this.uc = uc;
    }

    public void load(LocalDate date, List<Integer> calIds) {
        trigger.setValue(new Params(date, calIds));
    }

    private static class Params {
        final LocalDate date;
        final List<Integer> calIds;
        Params(LocalDate d, List<Integer> c) { date=d; calIds=c; }
    }
}