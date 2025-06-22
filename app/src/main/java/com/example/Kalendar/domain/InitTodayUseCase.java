package com.example.Kalendar.domain;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.CalendarRepository;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.repository.UserRepository;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class InitTodayUseCase {
    private static final String TAG = "InitTodayUseCase";

    private final UserRepository userRepo;
    private final CalendarRepository calRepo;
    private final DayRepository dayRepo;

    @Inject
    public InitTodayUseCase(UserRepository uR,
                            CalendarRepository cR,
                            DayRepository dR) {
        this.userRepo = uR;
        this.calRepo  = cR;
        this.dayRepo  = dR;
    }

    /**
     * Гарантированно инициализирует "сегодняшний" календарь и день для заданного userId.
     * Если пользователя нет — создаёт его.
     * Если календарей нет — создаёт дефолтный.
     * Если день не найден — создаёт запись DayEntity.
     */
    public void execute(int userId) {
        // 1) Проверяем и создаём UserEntity, если нужно
        UserEntity user = userRepo.getByIdSync(userId);
        if (user == null) {
            Log.i(TAG, "User с id=" + userId + " не найден — создаём дефолтного");
            user = new UserEntity();
            user.setId(userId);
            user.setName("DefaultUser");
            try {
                userRepo.insertSync(user);
            } catch (Exception e) {
                Log.e(TAG, "Не удалось создать UserEntity для id=" + userId, e);
                return;  // без пользователя дальше бессмысленно
            }
        }

        // 2) Получаем все календари этого пользователя
        List<CalendarEntity> all = calRepo.getByUserIdSync(userId);

        // 3) Если календарей нет — создаём дефолтный и повторяем чтение
        if (all.isEmpty()) {
            Log.i(TAG, "Календарей не найдено для userId=" + userId + " — создаём дефолтный");
            CalendarEntity cal = new CalendarEntity(
                    "Календарь по умолчанию",
                    System.currentTimeMillis(),
                    "#67BA80",
                    userId
            );
            try {
                calRepo.insertSync(cal);
            } catch (SQLiteConstraintException fk) {
                Log.e(TAG, "FK failed при вставке CalendarEntity userId=" + userId, fk);
                return;
            } catch (Exception ex) {
                Log.e(TAG, "Ошибка при insertSync(CalendarEntity)", ex);
                return;
            }

            all = calRepo.getByUserIdSync(userId);
            if (all.isEmpty()) {
                Log.e(TAG, "После вставки календарь всё ещё не найден для userId=" + userId);
                return;
            }
        }

        int calendarId = all.get(0).getId();
        Log.i(TAG, "Используем calendarId=" + calendarId + " для userId=" + userId);

        // 4) Инициализируем запись дня «сегодня» (midnight)
        long ts = midnightTimestamp();
        DayEntity day = dayRepo.getByTimestampAndCalendarIdSync(ts, calendarId);
        if (day == null) {
            Log.i(TAG, "День с timestamp=" + ts + " не найден — создаём новый");
            day = new DayEntity();
            day.setTimestamp(ts);
            day.setCalendarId(calendarId);
            try {
                dayRepo.insertSync(day);
            } catch (Exception e) {
                Log.e(TAG, "Не удалось сохранить DayEntity", e);
            }
        }
    }

    /** Возвращает миллисекунды начала текущих суток */
    private long midnightTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,   0);
        cal.set(Calendar.MINUTE,        0);
        cal.set(Calendar.SECOND,        0);
        cal.set(Calendar.MILLISECOND,   0);
        return cal.getTimeInMillis();
    }
}
