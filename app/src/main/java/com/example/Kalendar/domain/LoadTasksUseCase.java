package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.repository.TaskRepository;

import org.threeten.bp.LocalDate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoadTasksUseCase {
    private final TaskRepository repo;

    @Inject
    public LoadTasksUseCase(TaskRepository repo) {
        this.repo = repo;
    }

    public LiveData<List<TaskEntity>> execute(LocalDate date, int calendarId, String category) {
        return repo.getTasksForDate(date, calendarId, category);
    }
}