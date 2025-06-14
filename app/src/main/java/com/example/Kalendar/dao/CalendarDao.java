package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.CalendarEntity;

import java.util.List;

@Dao
public interface CalendarDao {
    @Query("SELECT * FROM calendars WHERE userId = :userId")
    List<CalendarEntity> getByUserIdSync(int userId);

    @Insert
    long insertSync(CalendarEntity calendar);

    @Query("SELECT * FROM calendars WHERE userId = :userId")
    LiveData<List<CalendarEntity>> getAllForUser(int userId);

    // Получить все календари конкретного пользователя
    @Query("SELECT * FROM calendars WHERE userId = :userId")
    LiveData<List<CalendarEntity>> getCalendarsForUserLiveData(int userId);

    // Получить один календарь по его id
    @Query("SELECT * FROM calendars WHERE id = :calendarId LIMIT 1")
    LiveData<CalendarEntity> getCalendarByIdLiveData(int calendarId);

    // Вставить новый календарь (при конфликте – заменить)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CalendarEntity calendar);

    // Обновить уже существующий календарь
    @Update
    void update(CalendarEntity calendar);

    // Удалить календарь
    @Delete
    void delete(CalendarEntity calendar);

    // Если нужен «синхронный» (не-LiveData) запрос для одного календаря:
    @Query("SELECT * FROM calendars WHERE id = :calendarId LIMIT 1")
    CalendarEntity getCalendarById(int calendarId);

    @Query("SELECT * FROM calendars")
    List<CalendarEntity> getAll();

    @Query("SELECT * FROM calendars WHERE id = :id AND userId = :userId LIMIT 1")
    CalendarEntity getById(int id, int userId);
    @Query("SELECT id FROM calendars WHERE title = :name AND userId = :userId LIMIT 1")
    int getIdByName(String name, int userId);

    @Query("SELECT * FROM calendars WHERE userId = :userId")
    List<CalendarEntity> getByUserId(int userId);

    @Query("SELECT * FROM calendars WHERE title = :title AND userId = :userId LIMIT 1")
    CalendarEntity getByTitleAndUserId(String title, int userId);
}

