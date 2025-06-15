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
    private final Executor executorIo;

    @Inject
    public CalendarRepository(CalendarDao calendarDao, Executor executorIo) {
        this.calendarDao = calendarDao;
        this.executorIo = executorIo;
    }

    public LiveData<List<CalendarEntity>> getAllCalendars(int userId) {
        return calendarDao.getCalendarsForUserLiveData(userId);
    }

    public void insert(CalendarEntity calendar) {
        executorIo.execute(() -> calendarDao.insert(calendar));
    }

    public void update(CalendarEntity calendar) {
        executorIo.execute(() -> calendarDao.update(calendar));
    }

    public void delete(CalendarEntity calendar) {
        executorIo.execute(() -> calendarDao.delete(calendar));
    }

    public List<CalendarEntity> getByUserIdSync(int userId) {
        return calendarDao.getByUserIdSync(userId);
    }


    public void insertSync(CalendarEntity cal) {
        executorIo.execute(() -> calendarDao.insertSync(cal));
    }

    public LiveData<List<CalendarEntity>> getAllForUser(int userId) {
        return calendarDao.getAllForUser(userId);
    }
    public LiveData<List<CalendarEntity>> getCalendarsForUser(int userId) {
        return calendarDao.getByUserIdLiveData(userId);
    }

    public CalendarEntity getByTitleAndUserIdSync(String title, int userId) {
        return calendarDao.getByTitleAndUserId(title, userId);
    }

    public long insertCalendarSync(CalendarEntity calendar) {
        return calendarDao.insertSync(calendar);
    }

    public void updateCalendarSync(CalendarEntity calendar) {
        calendarDao.updateSync(calendar);
    }

    public void deleteCalendarSync(CalendarEntity calendar) {
        calendarDao.deleteSync(calendar);
    }
}
