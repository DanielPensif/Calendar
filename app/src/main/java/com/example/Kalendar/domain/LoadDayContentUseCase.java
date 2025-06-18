package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.viewmodel.DayContent;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.List;

import javax.inject.Inject;

public class LoadDayContentUseCase {
    private final DayRepository repo;

    @Inject
    public LoadDayContentUseCase(DayRepository repo) {
        this.repo = repo;
    }

    public LiveData<DayContent> execute(LocalDate date, List<Integer> calendarIds) {
        MutableLiveData<DayContent> out = new MutableLiveData<>();
        new Thread(() -> {
            long midnight = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<TaskEntity> tasks  = repo.getTasksForDate(midnight, calendarIds);
            List<EventEntity> events = repo.getEventsForDate(midnight, calendarIds);
            out.postValue(new DayContent(tasks, events));
        }).start();
        return out;
    }
}