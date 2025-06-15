package com.example.Kalendar.domain;

import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.repository.CalendarRepository;

import javax.inject.Inject;

public class CreateCalendarUseCase {
    private final CalendarRepository repository;

    @Inject
    public CreateCalendarUseCase(CalendarRepository repository) {
        this.repository = repository;
    }

    public long execute(CalendarEntity calendar) {
        return repository.insertCalendarSync(calendar);
    }
}