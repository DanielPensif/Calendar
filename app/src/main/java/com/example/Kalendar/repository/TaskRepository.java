package com.example.Kalendar.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.ZoneId;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskRepository {
    private final TaskDao taskDao;
    private final DayDao dayDao;
    private final Executor ioExecutor;

    @Inject
    public TaskRepository(TaskDao taskDao, DayDao dayDao, Executor ioExecutor) {
        this.taskDao = taskDao;
        this.dayDao = dayDao;
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
    public LiveData<List<TaskEntity>> getTasksForDate(LocalDate date, int calendarId, String category) {
        MutableLiveData<List<TaskEntity>> out = new MutableLiveData<>();
        ioExecutor.execute(() -> {
            long tsStart = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
            List<DayEntity> days = dayDao.getByTimestamp(tsStart);
            List<TaskEntity> all = new ArrayList<>();
            for (DayEntity d : days) {
                if (calendarId != -1 && d.getCalendarId() != calendarId) continue;
                all.addAll(taskDao.getTasksForDay(d.getId()));
            }
            // фильтруем по категории
            List<TaskEntity> filtered = new ArrayList<>();
            for (TaskEntity t : all) {
                boolean matches = "Все".equals(category)
                        || ("Без категории".equals(category)
                        && (t.category == null || t.category.trim().isEmpty()))
                        || category.equals(t.category);
                if (matches) filtered.add(t);
            }
            out.postValue(filtered);
        });
        return out;
    }
}
