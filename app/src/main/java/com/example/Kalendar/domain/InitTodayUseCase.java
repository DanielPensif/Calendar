package com.example.Kalendar.domain;

import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.repository.CalendarRepository;
import com.example.Kalendar.repository.DayRepository;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class InitTodayUseCase {
    private final CalendarRepository calRepo;
    private final DayRepository dayRepo;

    @Inject
    public InitTodayUseCase(CalendarRepository cR, DayRepository dR) {
        this.calRepo = cR;
        this.dayRepo = dR;
    }

    public void execute(int userId) {
        List<CalendarEntity> all = calRepo.getByUserIdSync(userId);
        int calendarId;

        if (all.isEmpty()) {
            CalendarEntity cal = new CalendarEntity("Календарь по умолчанию",
                    System.currentTimeMillis(), "#67BA80", userId);
            calRepo.insertSync(cal);
            all = calRepo.getByUserIdSync(userId);
        }
        calendarId = all.get(0).id;

        long ts = midnightTimestamp();
        DayEntity day = dayRepo.getByTimestampAndCalendarIdSync(ts, calendarId);
        if (day == null) {
            day = new DayEntity();
            day.timestamp = ts;
            day.calendarId = calendarId;
            dayRepo.insertSync(day);
        }
    }

    private long midnightTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}