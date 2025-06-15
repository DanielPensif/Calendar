package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.models.EventEntity;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventRepository {

    private final EventDao eventDao;
    private final DayDao dayDao;
    private final Executor ioExecutor;

    @Inject
    public EventRepository(EventDao eventDao, DayDao dayDao, Executor ioExecutor) {
        this.eventDao = eventDao;
        this.dayDao = dayDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<EventEntity>> getEventsForDay(int dayId) {
        return eventDao.getEventsForDayLiveData(dayId);
    }

    public LiveData<List<EventEntity>> getEventsBetween(int start, int end) {
        return eventDao.getEventsBetweenLiveData(start, end);
    }

    public LiveData<List<EventEntity>> getEventsForCalendar(int calendarId) {
        return eventDao.getEventsForCalendarLiveData(calendarId);
    }

    public void insert(EventEntity event) {
        ioExecutor.execute(() -> eventDao.insert(event));
    }

    public void update(EventEntity event) {
        ioExecutor.execute(() -> eventDao.update(event));
    }

    public void delete(EventEntity event) {
        ioExecutor.execute(() -> eventDao.delete(event));
    }
    public List<EventEntity> getEventsForDate(long ts, List<Integer> calIds) {
        return eventDao.getEventsForDate(ts, calIds);
    }
    public List<EventEntity> getAll() { return eventDao.getAllEvents(); }
    public void save(EventEntity e) { eventDao.insert(e); }
    public void updateSync(EventEntity e) { eventDao.update(e); }
}
