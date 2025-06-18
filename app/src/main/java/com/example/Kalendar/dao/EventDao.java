package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.EventEntity;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events WHERE dayId IN (SELECT id FROM days WHERE timestamp = :ts AND calendarId IN (:calendarIds))")
    List<EventEntity> getEventsForDate(long ts, List<Integer> calendarIds);
    @Query("SELECT * FROM events")
    List<EventEntity> getAllEvents();

    // Получить все события для конкретного дня (по dayId)
    @Query("SELECT * FROM events WHERE dayId = :dayId")
    LiveData<List<EventEntity>> getEventsForDayLiveData(int dayId);

    // Получить все события для конкретного календаря (например, для построения списка)
    @Query("SELECT * FROM events WHERE calendarId = :calendarId")
    LiveData<List<EventEntity>> getEventsForCalendarLiveData(int calendarId);

    // Получить конкретное событие по его id
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    LiveData<EventEntity> getEventByIdLiveData(int eventId);

    // Синхронные варианты (если репозиторию/UseCase нужно получить объект без наблюдения)
    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    EventEntity getEventById(int eventId);

    @Query("SELECT * FROM events WHERE dayId = :dayId")
    List<EventEntity> getEventsForDay(int dayId);

    @Query("SELECT * FROM events WHERE calendarId = :calendarId")
    List<EventEntity> getEventsForCalendar(int calendarId);

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    EventEntity getById(int id);

    @Query("DELETE FROM events WHERE repeatRule = :rule")
    void deleteAllByRepeatRule(String rule);

    @Query("SELECT * FROM events")
    List<EventEntity> getAll();

    @Query("DELETE FROM events WHERE dayId NOT IN (SELECT id FROM days)")
    void deleteEventsWithMissingDay();

    @Query("SELECT * FROM events WHERE repeatRule = :rule LIMIT 1")
    EventEntity getFirstByRepeatRule(String rule);

    @Query("SELECT * FROM events WHERE dayId = :dayId")
    List<EventEntity> getByDayId(int dayId);

    @Query("SELECT * FROM events WHERE calendarId IN(:calendarIds)")
    List<EventEntity> getByCalendarIds(List<Integer> calendarIds);
    @Query("SELECT * FROM events WHERE dayId BETWEEN :start AND :end")
    LiveData<List<EventEntity>> getEventsBetweenLiveData(int start, int end);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(EventEntity event);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);

    // Пример сортировки по времени начала (если нужно)
    @Query("SELECT * FROM events WHERE dayId = :dayId ORDER BY timeStart ASC")
    LiveData<List<EventEntity>> getEventsForDaySortedLiveData(int dayId);
}
