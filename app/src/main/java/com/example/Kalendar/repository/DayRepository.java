package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DayRepository {
    private final DayDao dayDao;
    private final TaskDao taskDao;
    private final EventDao eventDao;
    private final Executor ioExecutor;

    @Inject
    public DayRepository(DayDao dayDao, TaskDao taskDao, EventDao eventDao, Executor ioExecutor) {
        this.dayDao = dayDao;
        this.taskDao = taskDao;
        this.eventDao = eventDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<DayEntity>> getDaysForCalendar(int calendarId) {
        return dayDao.getDaysForCalendarLiveData(calendarId);
    }
    public LiveData<DayEntity> getDayByTimestampAndCalendarId(long timestamp, int calendarId){
        return dayDao.getDayByTimestampAndCalendarIdLiveData(timestamp, calendarId);
    }

    public void insert(DayEntity day) {
        ioExecutor.execute(() -> dayDao.insert(day));
    }

    public void update(DayEntity day) {
        ioExecutor.execute(() -> dayDao.update(day));
    }

    public void delete(DayEntity day) {
        ioExecutor.execute(() -> dayDao.delete(day));
    }
    public DayEntity getByTimestampAndCalendarIdSync(long ts, int calId) {
        return dayDao.getByTimestampAndCalendarIdSync(ts, calId);
    }

    public long insertSync(DayEntity day) {
        return dayDao.insertSync(day);
    }

    public List<DayEntity> getByTimestampAndCalendarIds(long ts, List<Integer> cIds) {
        return dayDao.getByTimestampAndCalendarIds(ts, cIds);
    }
    public List<DayEntity> getByCalendarIdsSync(List<Integer> calendarIds) {
        return dayDao.getByCalendarIds(calendarIds);
    }
    public List<DayEntity> getByCalendarIdSync(int calendarId) {
        return dayDao.getByCalendarId(calendarId);
    }
    public List<DayEntity> getByTimestampAndCalendarIdsSync(long timestamp, List<Integer> calendarIds) {
        return dayDao.getByTimestampAndCalendarIds(timestamp, calendarIds);
    }
    public DayEntity getByIdSync(int id) {
        return dayDao.getById(id);
    }

    public List<TaskEntity> getTasksForDaySync(int dayId) {
        return taskDao.getTasksForDay(dayId);
    }
    public List<EventEntity> getEventsForDaySync(int dayId) {
        return eventDao.getEventsForDay(dayId);
    }
    public List<EventEntity> getEventsByCalendarIdsSync(List<Integer> calendarIds) {
        return eventDao.getByCalendarIds(calendarIds);
    }
    public List<DayEntity> getById(int id) {
        return Collections.singletonList(dayDao.getById(id));
    }
}
