package com.example.Kalendar.domain;

import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.repository.CalendarRepository;

import javax.inject.Inject;

public class DeleteCalendarUseCase {
    private final CalendarRepository repository;

    @Inject
    public DeleteCalendarUseCase(CalendarRepository repository) {
        this.repository = repository;
    }

    public void execute(CalendarEntity calendar) {
        repository.deleteCalendarSync(calendar);
    }
}