package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.CalendarDao;
import com.example.Kalendar.models.CalendarEntity;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CalendarRepository {

    private final CalendarDao calendarDao;
    private final Executor ioExecutor;

    @Inject
    public CalendarRepository(CalendarDao calendarDao, Executor ioExecutor) {
        this.calendarDao = calendarDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<CalendarEntity>> getAllCalendars(int userId) {
        return calendarDao.getCalendarsForUserLiveData(userId);
    }

    public void insert(CalendarEntity calendar) {
        ioExecutor.execute(() -> calendarDao.insert(calendar));
    }

    public void update(CalendarEntity calendar) {
        ioExecutor.execute(() -> calendarDao.update(calendar));
    }

    public void delete(CalendarEntity calendar) {
        ioExecutor.execute(() -> calendarDao.delete(calendar));
    }
}
