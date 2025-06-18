package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.repository.EventRepository;
import com.example.Kalendar.models.EventEntity;

import org.threeten.bp.LocalDate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoadEventsUseCase {
    private final EventRepository repo;

    @Inject
    public LoadEventsUseCase(EventRepository repo) {
        this.repo = repo;
    }

    public LiveData<List<EventEntity>> execute(
            LocalDate date,
            int calendarId,
            String category
    ) {
        return repo.getEventsForDate(date, calendarId, category);
    }
}