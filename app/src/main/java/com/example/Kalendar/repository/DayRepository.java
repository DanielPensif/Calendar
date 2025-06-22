package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public DayEntity getOrCreateDaySync(long timestamp, int calendarId) {
        DayEntity d = dayDao.getByTimestampAndCalendarId(timestamp, calendarId);
        if (d == null) {
            d = new DayEntity();
            d.setTimestamp(timestamp);
            d.setCalendarId(calendarId);
            d.setId((int) dayDao.insert(d));
        }
        return d;
    }
    public List<TaskEntity> getTasksForDate(long dayStartTs, List<Integer> calendarIds) {
        List<TaskEntity> out = new ArrayList<>();
        List<DayEntity> days = dayDao.getByTimestampAndCalendarIds(dayStartTs, calendarIds);
        for (DayEntity d : days) {
            out.addAll(taskDao.getTasksForDay(d.getId()));
        }
        return out;
    }
    public List<EventEntity> getEventsForDate(long dayStartTs, List<Integer> calendarIds) {
        List<EventEntity> out = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        // 1) оригинальные
        List<DayEntity> days = dayDao.getByTimestampAndCalendarIds(dayStartTs, calendarIds);
        for (DayEntity d : days) {
            for (EventEntity e : eventDao.getEventsForDay(d.getId())) {
                out.add(e);
                seen.add(e.id);
            }
        }

        // 2) виртуальные (повторения)
        LocalDate targetDate = Instant.ofEpochMilli(dayStartTs)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<EventEntity> all = eventDao.getByCalendarIds(calendarIds);
        for (EventEntity e : all) {
            if (seen.contains(e.id) || e.repeatRule == null || e.repeatRule.isEmpty()) continue;
            DayEntity base = dayDao.getById(e.dayId);
            if (base == null) continue;
            LocalDate startDate = Instant.ofEpochMilli(base.getTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (EventUtils.occursOnDate(e, targetDate, startDate)) {
                out.add(e);
            }
        }

        return out;
    }
}
