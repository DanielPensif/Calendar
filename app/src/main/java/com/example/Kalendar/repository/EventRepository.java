package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
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
    public LiveData<List<EventEntity>> getEventsForDate(
            LocalDate date,
            int calendarId,
            String category
    ) {
        MutableLiveData<List<EventEntity>> out = new MutableLiveData<>();
        ioExecutor.execute(() -> {
            long tsStart = date.atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            // 1) События, привязанные к дню
            List<DayEntity> days = dayDao.getByTimestamp(tsStart);
            List<EventEntity> collected = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();

            for (DayEntity d : days) {
                if (calendarId != -1 && d.calendarId != calendarId) continue;
                List<EventEntity> evs = eventDao.getEventsForDay(d.id);
                for (EventEntity e : evs) {
                    boolean occurs = true;
                    if (e.repeatRule != null && !e.repeatRule.isEmpty()) {
                        LocalDate startDate = Instant.ofEpochMilli(d.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        occurs = EventUtils.occursOnDate(e, date, startDate);
                    }
                    if (occurs) {
                        collected.add(e);
                        seen.add(e.id);
                    }
                }
            }

            // 2) Повторяющиеся события без конкретного дня (виртуальные копии)
            for (EventEntity e : eventDao.getAll()) {
                if (seen.contains(e.id)) continue;
                if (calendarId != -1 && e.calendarId != calendarId) continue;
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;

                DayEntity base = dayDao.getById(e.dayId);
                if (base == null) continue;
                LocalDate startDate = Instant.ofEpochMilli(base.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (EventUtils.occursOnDate(e, date, startDate)) {
                    // клонируем, но сохраняем тот же id, чтобы DiffUtil разграничивал
                    EventEntity copy = new EventEntity();
                    copy.id                 = e.id;
                    copy.title              = e.title;
                    copy.timeStart          = e.timeStart;
                    copy.timeEnd            = e.timeEnd;
                    copy.allDay             = e.allDay;
                    copy.repeatRule         = e.repeatRule;
                    copy.excludedDates      = e.excludedDates;
                    copy.category           = e.category;
                    copy.location           = e.location;
                    copy.description        = e.description;
                    copy.done               = e.done;
                    copy.dayId              = e.dayId;
                    copy.calendarId         = e.calendarId;
                    copy.notifyOnStart      = e.notifyOnStart;
                    copy.earlyReminderEnabled = e.earlyReminderEnabled;
                    copy.earlyReminderHour  = e.earlyReminderHour;
                    copy.earlyReminderMinute= e.earlyReminderMinute;
                    copy.userId = e.userId;
                    collected.add(copy);
                }
            }

            // 3) Фильтрация по категории
            List<EventEntity> filtered = new ArrayList<>();
            for (EventEntity e : collected) {
                boolean keep =
                        "Все".equals(category)
                                || ("Без категории".equals(category)
                                && (e.category == null || e.category.trim().isEmpty()))
                                || category.equals(e.category);
                if (keep) filtered.add(e);
            }

            out.postValue(filtered);
        });
        return out;
    }
}
