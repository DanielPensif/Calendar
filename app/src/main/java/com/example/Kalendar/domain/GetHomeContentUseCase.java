package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.repository.CalendarRepository;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.utils.EventUtils;
import com.example.Kalendar.viewmodel.HomeContent;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class GetHomeContentUseCase {
    private final CalendarRepository calRepo;
    private final DayRepository dayRepo;
    private final AppDatabase db;

    @Inject
    public GetHomeContentUseCase(CalendarRepository c, DayRepository d, AppDatabase db) {
        this.calRepo = c;
        this.dayRepo = d;
        this.db      = db;
    }

    public LiveData<HomeContent> execute(int userId) {
        MutableLiveData<HomeContent> result = new MutableLiveData<>();
        new Thread(() -> {
            // 1. Список календарей
            List<Integer> cIds = new ArrayList<>();
            for (CalendarEntity c : calRepo.getByUserIdSync(userId)) {
                cIds.add(c.id);
            }

            // 2. Сегодняшний ts
            long tsToday = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            // 3. Дни
            List<DayEntity> days =
                    dayRepo.getByTimestampAndCalendarIds(tsToday, cIds);

            List<TaskEntity> tasks = new ArrayList<>();
            List<EventEntity> events = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();

            // 4. Собираем по дню создания
            for (DayEntity day : days) {
                tasks.addAll(db.taskDao().getTasksForDay(day.id));
                for (EventEntity e : db.eventDao().getEventsForDay(day.id)) {
                    boolean occurs = (e.repeatRule == null || e.repeatRule.isEmpty())
                            || EventUtils.occursOnDate(e,
                            LocalDate.now(),
                            Instant.ofEpochMilli(day.timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate());
                    if (occurs) {
                        events.add(e);
                        seen.add(e.id);
                    }
                }
            }

            // 5. Повторы
            List<EventEntity> allE = db.eventDao().getByCalendarIds(cIds);
            LocalDate today = LocalDate.now();
            for (EventEntity e : allE) {
                if (seen.contains(e.id) || e.repeatRule == null || e.repeatRule.isEmpty()) continue;
                DayEntity base = db.dayDao().getById(e.dayId);
                if (base == null) continue;
                LocalDate start = Instant.ofEpochMilli(base.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (EventUtils.occursOnDate(e, today, start)) {
                    events.add(e);
                }
            }

            result.postValue(new HomeContent(tasks, events));
        }).start();
        return result;
    }
}