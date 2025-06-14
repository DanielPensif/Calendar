package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.models.DayEntity;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DayRepository {

    private final DayDao dayDao;
    private final Executor ioExecutor;

    @Inject
    public DayRepository(DayDao dayDao, Executor ioExecutor) {
        this.dayDao = dayDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<DayEntity>> getDaysForCalendar(int calendarId) {
        return dayDao.getDaysForCalendarLiveData(calendarId);
    }
    public LiveData<DayEntity> getDayByTimestampAndCalendarId(long timestamp, int calendarId){
        return dayDao.getDayByTimestampAndCalendarIdLiveData(timestamp, calendarId);
    }

    public void insert(DayEntity day) {
        ioExecutor.execute(() -> dayDao.insert(day));
    }

    public void update(DayEntity day) {
        ioExecutor.execute(() -> dayDao.update(day));
    }

    public void delete(DayEntity day) {
        ioExecutor.execute(() -> dayDao.delete(day));
    }
}
