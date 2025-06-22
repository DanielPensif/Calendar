package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.LoadEventsUseCase;
import com.example.Kalendar.models.EventEntity;

import org.threeten.bp.LocalDate;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EventsViewModel extends ViewModel {
    private final LoadEventsUseCase useCase;

    // Параметры фильтра: дата, календарь, категория
    private final MutableLiveData<Params> params =
            new MutableLiveData<>(new Params(LocalDate.now(), -1, "Все"));

    // Список событий — инициализируем в конструкторе
    public final LiveData<List<EventEntity>> events;

    @Inject
    public EventsViewModel(LoadEventsUseCase useCase) {
        this.useCase = useCase;
        // Переносим сюда, чтобы useCase уже был установлен
        this.events = Transformations.switchMap(
                params,
                p -> this.useCase.execute(p.date, p.calendarId, p.category)
        );
    }

    public void setDate(LocalDate date)      { update(date, getCalendarId(), getCategory()); }
    public void setCalendarId(int id)        { update(getDate(), id, getCategory()); }
    public void setCategory(String category) { update(getDate(), getCalendarId(), category); }

    private void update(LocalDate date, int calId, String category) {
        Params newP = new Params(date, calId, category);
        if (!newP.equals(params.getValue())) {
            params.setValue(newP);
        }
    }

    public LocalDate getDate()    { return params.getValue().date; }
    public int getCalendarId()    { return params.getValue().calendarId; }
    public String getCategory()   { return params.getValue().category; }

    private static class Params {
        final LocalDate date;
        final int calendarId;
        final String category;
        Params(LocalDate d, int c, String cat) {
            date = d; calendarId = c; category = cat;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Params)) return false;
            Params p = (Params)o;
            return calendarId == p.calendarId
                    && Objects.equals(date, p.date)
                    && Objects.equals(category, p.category);
        }
    }
}