package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.Kalendar.domain.CreateCalendarUseCase;
import com.example.Kalendar.domain.DeleteCalendarUseCase;
import com.example.Kalendar.domain.LoadCalendarsUseCase;
import com.example.Kalendar.domain.UpdateCalendarUseCase;
import com.example.Kalendar.models.CalendarEntity;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarManagerViewModel extends ViewModel {
    public final LiveData<List<CalendarEntity>> calendars;

    private final LoadCalendarsUseCase loadCalendarsUseCase;
    private final CreateCalendarUseCase createCalendarUseCase;
    private final UpdateCalendarUseCase updateCalendarUseCase;
    private final DeleteCalendarUseCase deleteCalendarUseCase;

    private final MutableLiveData<Integer> userIdParam = new MutableLiveData<>();

    @Inject
    public CalendarManagerViewModel(
            LoadCalendarsUseCase loadCalendarsUseCase,
            CreateCalendarUseCase createCalendarUseCase,
            UpdateCalendarUseCase updateCalendarUseCase,
            DeleteCalendarUseCase deleteCalendarUseCase
    ) {
        this.loadCalendarsUseCase = loadCalendarsUseCase;
        this.createCalendarUseCase = createCalendarUseCase;
        this.updateCalendarUseCase = updateCalendarUseCase;
        this.deleteCalendarUseCase = deleteCalendarUseCase;

        // Перезагружаем список календарей при смене userIdParam
        this.calendars = Transformations.switchMap(userIdParam, loadCalendarsUseCase::execute);
    }

    public void setUserId(int userId) {
        userIdParam.setValue(userId);
    }
    public void createCalendar(CalendarEntity e) {
        new Thread(() -> createCalendarUseCase.execute(e)).start();
        setUserId(userIdParam.getValue());
    }
    public void updateCalendar(CalendarEntity e) {
        new Thread(() -> updateCalendarUseCase.execute(e)).start();
    }
    public void deleteCalendar(CalendarEntity e) {
        new Thread(() -> deleteCalendarUseCase.execute(e)).start();
        setUserId(userIdParam.getValue());
    }
}