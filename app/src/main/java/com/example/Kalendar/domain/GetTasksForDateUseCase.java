package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.repository.TaskRepository;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class GetTasksForDateUseCase {
    private final DayRepository dayRepo;
    private final TaskRepository taskRepo;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Inject
    public GetTasksForDateUseCase(DayRepository dr, TaskRepository tr) {
        this.dayRepo = dr;
        this.taskRepo = tr;
    }

    public LiveData<List<TaskEntity>> execute(LocalDate date, int calendarId) {
        MutableLiveData<List<TaskEntity>> live = new MutableLiveData<>();
        exec.execute(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            int dayId = dayRepo.getOrCreateDaySync(ts, calendarId).getId();
            List<TaskEntity> list = taskRepo.getTasksForDaySync(dayId);
            live.postValue(list);
        });
        return live;
    }
}