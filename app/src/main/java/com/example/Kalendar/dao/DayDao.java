package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.DayEntity;

import java.util.List;

@Dao
public interface DayDao {
    @Insert
    long insert(DayEntity day);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDay(DayEntity day);

    @Query("SELECT * FROM days WHERE calendarId = :calendarId")
    List<DayEntity> getByCalendarId(int calendarId);

    @Query("SELECT * FROM days WHERE timestamp = :timestamp")
    List<DayEntity> getByTimestamp(long timestamp);


    @Query("SELECT * FROM days WHERE timestamp = :timestamp AND calendarId = :calendarId LIMIT 1")
    DayEntity getByTimestampAndCalendarId(long timestamp, int calendarId);


    @Query("SELECT * FROM days")
    List<DayEntity> getAll();

    @Query("SELECT * FROM days WHERE id = :id LIMIT 1")
    DayEntity getById(int id);

    @Query("SELECT * FROM days")
    List<DayEntity> getAllDays();

    @Query("SELECT * FROM days WHERE timestamp BETWEEN :start AND :end")
    List<DayEntity> getDaysBetween(long start, long end);

    @Update
    void update(DayEntity day);
}

