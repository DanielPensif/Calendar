package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(TaskEntity task);
    @Query("SELECT * FROM tasks WHERE dayId = :dayId")
    List<TaskEntity> getTasksForDay(int dayId);

    @Query("SELECT EXISTS(SELECT 1 FROM tasks WHERE dayId = :dayId LIMIT 1)")
    boolean hasTasksForDay(int dayId);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity getById(int id);

    @Query("SELECT * FROM tasks WHERE dayId = :dayId")
    List<TaskEntity> getTasksByDayId(int dayId);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

}

