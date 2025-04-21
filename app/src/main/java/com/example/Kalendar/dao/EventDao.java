package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.EventEntity;

import java.util.List;

@Dao
public interface EventDao {
    @Insert
    long insert(EventEntity task);
    @Query("SELECT * FROM events WHERE dayId = :dayId")
    List<EventEntity> getEventsForDay(int dayId);

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    EventEntity getById(int id);

    @Update
    void update(EventEntity event);

    @Delete
    void delete(EventEntity event);

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


}

