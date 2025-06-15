package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.repository.CalendarRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class LoadCalendarsUseCase {
    private final CalendarRepository repo;

    @Inject
    public LoadCalendarsUseCase(CalendarRepository repo) {
        this.repo = repo;
    }

    public LiveData<List<CalendarEntity>> execute(int userId) {
        return repo.getCalendarsForUser(userId);
    }
}