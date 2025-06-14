package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.models.EventEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Репозиторий для работы с событиями.
 * Хранит ссылку на EventDao и Executor для асинхронных операций.
 */
@Singleton
public class EventRepository {

    private final EventDao eventDao;
    private final Executor ioExecutor;

    @Inject
    public EventRepository(EventDao eventDao, Executor ioExecutor) {
        this.eventDao = eventDao;
        this.ioExecutor = ioExecutor;
    }

    /**
     * Возвращает LiveData со списком событий за конкретную дату.
     */
    public LiveData<List<EventEntity>> getEventsForDay(int dayId) {
        return eventDao.getEventsForDayLiveData(dayId);
    }

    /**
     * Возвращает LiveData со списком событий в заданном диапазоне дат.
     */
    public LiveData<List<EventEntity>> getEventsBetween(int start, int end) {
        return eventDao.getEventsBetweenLiveData(start, end);
    }

    public LiveData<List<EventEntity>> getEventsForCalendar(int calendarId) {
        return eventDao.getEventsForCalendarLiveData(calendarId);
    }

    /**
     * Вставка нового события (асинхронно).
     */
    public void insert(EventEntity event) {
        ioExecutor.execute(() -> eventDao.insert(event));
    }

    /**
     * Обновление события (асинхронно).
     */
    public void update(EventEntity event) {
        ioExecutor.execute(() -> eventDao.update(event));
    }

    /**
     * Удаление события (асинхронно).
     */
    public void delete(EventEntity event) {
        ioExecutor.execute(() -> eventDao.delete(event));
    }
}
