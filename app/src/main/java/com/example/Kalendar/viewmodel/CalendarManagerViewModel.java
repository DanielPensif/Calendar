package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

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
    // LiveData для списка календарей
    public final LiveData<List<CalendarEntity>> calendars;

    private final CreateCalendarUseCase createCalendarUseCase;
    private final UpdateCalendarUseCase updateCalendarUseCase;
    private final DeleteCalendarUseCase deleteCalendarUseCase;

    private final MutableLiveData<Integer> userIdParam = new MutableLiveData<>();
    private int currentUserId = -1;

    @Inject
    public CalendarManagerViewModel(
            LoadCalendarsUseCase loadCalendarsUseCase,
            CreateCalendarUseCase createCalendarUseCase,
            UpdateCalendarUseCase updateCalendarUseCase,
            DeleteCalendarUseCase deleteCalendarUseCase
    ) {
        this.createCalendarUseCase = createCalendarUseCase;
        this.updateCalendarUseCase = updateCalendarUseCase;
        this.deleteCalendarUseCase = deleteCalendarUseCase;

        // Перезагружаем список при изменении userIdParam
        this.calendars = Transformations.switchMap(
                userIdParam,
                loadCalendarsUseCase::execute
        );
    }

    public void init(int userId) {
        currentUserId = userId;
        userIdParam.setValue(userId);
    }

    public void createCalendar(CalendarEntity entity) {
        new Thread(() -> {
            createCalendarUseCase.execute(entity);
            userIdParam.postValue(currentUserId);
        }).start();
    }

    public void updateCalendar(CalendarEntity entity) {
        new Thread(() -> {
            updateCalendarUseCase.execute(entity);
            userIdParam.postValue(currentUserId);
        }).start();
    }

    public void deleteCalendar(CalendarEntity entity) {
        new Thread(() -> {
            deleteCalendarUseCase.execute(entity);
            userIdParam.postValue(currentUserId);
        }).start();
    }
}