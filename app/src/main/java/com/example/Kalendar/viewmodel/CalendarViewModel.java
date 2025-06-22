package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.GetCalendarContentUseCase;

import org.threeten.bp.LocalDate;

import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {
    private final GetCalendarContentUseCase uc;
    private final MutableLiveData<Params> params = new MutableLiveData<>();
    private final LiveData<CalendarContent> content;

    private final MutableLiveData<String> quoteLive = new MutableLiveData<>();

    @Inject
    public CalendarViewModel(GetCalendarContentUseCase uc) {
        this.uc = uc;

        content = Transformations.switchMap(params, p ->
                uc.execute(p.userId, p.calendarId, p.monthStart)
        );
    }

    public LiveData<CalendarContent> loadCalendar(int userId, int calendarId, LocalDate monthStart) {
        return uc.execute(userId, calendarId, monthStart);
    }

    public LiveData<CalendarContent> getContent() {
        return content;
    }

    public LiveData<String> getQuote() {
        // Тот же массив QUOTES
        String[] QUOTES = {
                "Каждый день — это шанс начать заново.",
                "Успех — это сумма маленьких усилий, повторяемых изо дня в день.",
                "Сложности делают тебя сильнее.",
                "Сначала ты работаешь на результат, потом результат работает на тебя.",
                "Твоя цель — не быть лучше других, а быть лучше вчерашнего себя."
        };
        quoteLive.postValue(
                QUOTES[new Random().nextInt(QUOTES.length)]
        );
        return quoteLive;
    }

    private static class Params {
        final int userId, calendarId;
        final LocalDate monthStart;
        Params(int u, int c, LocalDate m) { userId = u; calendarId = c; monthStart = m; }
    }
}