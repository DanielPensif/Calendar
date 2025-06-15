package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.models.TaskEntity;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskRepository {

    private final TaskDao taskDao;
    private final Executor ioExecutor;

    @Inject
    public TaskRepository(TaskDao taskDao, Executor ioExecutor) {
        this.taskDao = taskDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<TaskEntity>> getTasksForDay(int dayId) {
        return taskDao.getTasksForDayLiveData(dayId);
    }
    public LiveData<List<TaskEntity>> getTasksForCalendar(int calendarId) {
        return taskDao.getTasksForCalendarLiveData(calendarId);
    }
    public void insert(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.insert(task));
    }

    public void update(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.update(task));
    }

    public void delete(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.delete(task));
    }
    public List<TaskEntity> getTasksForDaySync(int dayId) {
        return taskDao.getTasksForDay(dayId);
    }
    public List<TaskEntity> getTasksForDate(long ts, List<Integer> calIds) {
        return taskDao.getTasksForDate(ts, calIds);
    }
    public void save(TaskEntity t) { taskDao.insert(t); }
    public void updateSync(TaskEntity t) { taskDao.update(t); }
}
