package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.Kalendar.models.CalendarEntity;

import java.util.List;

@Dao
public interface CalendarDao {
    @Insert
    long insert(CalendarEntity calendar);

    @Query("SELECT * FROM calendars")
    List<CalendarEntity> getAll();

    @Query("SELECT * FROM calendars WHERE id = :id AND userId = :userId LIMIT 1")
    CalendarEntity getById(int id, int userId);

    @androidx.room.Update
    void update(CalendarEntity calendar);

    @androidx.room.Delete
    void delete(CalendarEntity calendar);

    @Query("SELECT id FROM calendars WHERE title = :name AND userId = :userId LIMIT 1")
    int getIdByName(String name, int userId);

    @Query("SELECT * FROM calendars WHERE userId = :userId")
    List<CalendarEntity> getByUserId(int userId);

    @Query("SELECT * FROM calendars WHERE userId = :userId")
    List<CalendarEntity> getAllForUser(int userId);
    @Query("SELECT * FROM calendars WHERE title = :title AND userId = :userId LIMIT 1")
    CalendarEntity getByTitleAndUserId(String title, int userId);
}
