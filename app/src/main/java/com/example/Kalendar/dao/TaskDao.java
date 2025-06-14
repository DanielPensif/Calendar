package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    // Получить все задачи для конкретного дня
    @Query("SELECT * FROM tasks WHERE dayId = :dayId")
    LiveData<List<TaskEntity>> getTasksForDayLiveData(int dayId);

    // Получить задачи для календаря (косвенно), если нужно
    @Query("SELECT * FROM tasks WHERE calendarId = :calendarId")
    LiveData<List<TaskEntity>> getTasksForCalendarLiveData(int calendarId);

    // Получить одну задачу по её id
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    LiveData<TaskEntity> getTaskByIdLiveData(int taskId);

    // Синхронные варианты
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity getTaskById(int taskId);

    @Query("SELECT * FROM tasks WHERE dayId = :dayId")
    List<TaskEntity> getTasksForDay(int dayId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("SELECT EXISTS(SELECT 1 FROM tasks WHERE dayId = :dayId LIMIT 1)")
    boolean hasTasksForDay(int dayId);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    TaskEntity getById(int id);

    @Query("SELECT * FROM tasks WHERE dayId = :dayId")
    List<TaskEntity> getTasksByDayId(int dayId);

    @Query("SELECT * FROM tasks")
    List<TaskEntity> getAllTasks();

    @Query("SELECT COUNT(*) FROM tasks WHERE dayId = :dayId AND done = true")
    int getCompletedTaskCountForDay(int dayId);
    @Query("SELECT COUNT(*) FROM tasks WHERE dayId = :dayId AND done = true")
    int getCompletedTaskCountByDayId(int dayId);

    @Query("SELECT COUNT(*) FROM tasks WHERE dayId = :dayId")
    int getTotalTaskCountByDayId(int dayId);
}

