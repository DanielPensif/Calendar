package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.LoadTasksUseCase;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.LocalDate;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class TasksViewModel extends ViewModel {
    private LoadTasksUseCase useCase;
    private final MutableLiveData<Params> params = new MutableLiveData<>(new Params(LocalDate.now(), -1, "Все"));

    public final LiveData<List<TaskEntity>> tasks = Transformations.switchMap(params, p ->
            useCase.execute(p.date, p.calendarId, p.category)
    );

    @Inject
    public TasksViewModel(LoadTasksUseCase useCase) {
        this.useCase = useCase;
    }

    public void setDate(LocalDate date) { update(date, getCalendarId(), getCategory()); }
    public void setCalendarId(int id)  { update(getDate(), id, getCategory()); }
    public void setCategory(String cat){ update(getDate(), getCalendarId(), cat); }

    private void update(LocalDate d, int cal, String cat) {
        Params p = new Params(d, cal, cat);
        if (!p.equals(params.getValue())) params.setValue(p);
    }

    public LocalDate getDate()    { return params.getValue().date; }
    public int getCalendarId()    { return params.getValue().calendarId; }
    public String getCategory()   { return params.getValue().category; }

    private static class Params {
        final LocalDate date;
        final int calendarId;
        final String category;
        Params(LocalDate d, int c, String cat) { date = d; calendarId = c; category = cat; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Params)) return false;
            Params p = (Params) o;
            return calendarId == p.calendarId
                    && Objects.equals(date, p.date)
                    && Objects.equals(category, p.category);
        }
    }
}