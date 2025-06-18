package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.DayEntity;

import java.util.List;

@Dao
public interface DayDao {
    @Query("SELECT * FROM days WHERE timestamp = :ts AND calendarId = :calId")
    DayEntity getByTimestampAndCalendarIdSync(long ts, int calId);

    @Insert
    long insertSync(DayEntity day);

    @Query("SELECT * FROM days WHERE timestamp = :ts AND calendarId IN(:cIds)")
    List<DayEntity> getByTimestampAndCalendarIds(long ts, List<Integer> cIds);

    @Query("SELECT * FROM days WHERE calendarId IN(:cIds)")
    List<DayEntity> getByDayIdsForCalendars(List<Integer> cIds);

    @Query("SELECT * FROM days WHERE id = :id")
    DayEntity getById(int id);

    // Получить все дни для заданного календаря
    @Query("SELECT * FROM days WHERE calendarId = :calendarId")
    LiveData<List<DayEntity>> getDaysForCalendarLiveData(int calendarId);

    // Найти конкретный день по timestamp и calendarId (например, для отметки выполненного дня)
    @Query("SELECT * FROM days WHERE timestamp = :timestamp AND calendarId = :calendarId LIMIT 1")
    LiveData<DayEntity> getDayByTimestampAndCalendarIdLiveData(long timestamp, int calendarId);

    // Синхронный вариант (без LiveData), если понадобится
    @Query("SELECT * FROM days WHERE timestamp = :timestamp AND calendarId = :calendarId LIMIT 1")
    DayEntity getDayByTimestampAndCalendarId(long timestamp, int calendarId);

    @Query("SELECT * FROM days WHERE calendarId = :calendarId")
    List<DayEntity> getByCalendarId(int calendarId);

    @Query("SELECT * FROM days WHERE timestamp = :timestamp")
    List<DayEntity> getByTimestamp(long timestamp);


    @Query("SELECT * FROM days WHERE timestamp = :timestamp AND calendarId = :calendarId LIMIT 1")
    DayEntity getByTimestampAndCalendarId(long timestamp, int calendarId);
    @Query("SELECT * FROM days")
    List<DayEntity> getAll();

    @Query("SELECT * FROM days WHERE timestamp BETWEEN :start AND :end")
    List<DayEntity> getDaysBetween(long start, long end);

    @Query("SELECT * FROM days WHERE calendarId IN(:calendarIds)")
    List<DayEntity> getByCalendarIds(List<Integer> calendarIds);
    @Query("SELECT * FROM days WHERE timestamp BETWEEN :start AND :end AND calendarId IN (:calendarIds)")
    List<DayEntity> getDaysBetweenForCalendars(long start, long end, List<Integer> calendarIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DayEntity day);

    @Update
    void update(DayEntity day);

    @Delete
    void delete(DayEntity day);
}
