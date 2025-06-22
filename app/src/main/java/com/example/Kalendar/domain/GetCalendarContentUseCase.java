package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.viewmodel.CalendarContent;
import com.example.Kalendar.repository.CalendarRepository;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.utils.EventUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.util.*;
import java.util.function.BiFunction;

import javax.inject.Inject;
import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class GetCalendarContentUseCase {
    private final CalendarRepository calRepo;
    private final DayRepository dayRepo;
    private final com.example.Kalendar.db.AppDatabase db;

    @Inject
    public GetCalendarContentUseCase(
            CalendarRepository calRepo,
            DayRepository dayRepo,
            com.example.Kalendar.db.AppDatabase db
    ) {
        this.calRepo = calRepo;
        this.dayRepo = dayRepo;
        this.db      = db;
    }

    public LiveData<CalendarContent> execute(int userId, int calendarId, LocalDate monthStart) {
        MutableLiveData<CalendarContent> live = new MutableLiveData<>();
        new Thread(() -> {
            // 1. Загрузить все календари пользователя и подготовить список для спиннера
            List<CalendarEntity> rawCals = calRepo.getByUserIdSync(userId);
            List<CalendarEntity> spinnerCals = new ArrayList<>();
            spinnerCals.add(null); // позиция 0 — "Все календари"
            spinnerCals.addAll(rawCals);

            // 2. Построить colorMap
            Map<Integer, String> colorMap = new HashMap<>();
            for (var c : rawCals) colorMap.put(c.getId(), c.getColorHex());

            // 3. Сформировать список дней месяца (42 или 35)
            List<LocalDate> daysInMonth = new ArrayList<>();
            LocalDate firstOfMonth = monthStart.withDayOfMonth(1);
            int shift = firstOfMonth.getDayOfWeek().getValue() - 1; // 1=понедельник
            LocalDate start = firstOfMonth.minusDays(shift);
            for (int i = 0; i < 42; i++) daysInMonth.add(start.plusDays(i));
            if (daysInMonth.subList(35, 42).stream()
                    .allMatch(d -> d.getMonthValue() != monthStart.getMonthValue())) {
                daysInMonth = daysInMonth.subList(0, 35);
            }

            // 4. Загрузить DayEntity
            List<Integer> calendarIds;
            if (calendarId == -1) {
                calendarIds = new ArrayList<>();
                for (var c : rawCals) calendarIds.add(c.getId());
            } else {
                calendarIds = Collections.singletonList(calendarId);
            }
            List<com.example.Kalendar.models.DayEntity> days =
                    dayRepo.getByCalendarIdsSync(calendarIds);

            // 5. Построить activeDayCalendars
            Map<LocalDate, Set<Integer>> activeDayCalendars = new HashMap<>();
            for (var day : days) {
                LocalDate date = Instant.ofEpochMilli(day.getTimestamp())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                boolean hasTask = !db.taskDao().getTasksForDay(day.getId()).isEmpty();
                boolean hasEvent = db.eventDao().getEventsForDay(day.getId()).stream().anyMatch(e ->
                        (e.repeatRule == null || e.repeatRule.isEmpty()) ||
                                EventUtils.occursOnDate(e, date,
                                        Instant.ofEpochMilli(day.getTimestamp())
                                                .atZone(ZoneId.systemDefault()).toLocalDate())
                );
                if (hasTask || hasEvent) {
                    activeDayCalendars
                            .computeIfAbsent(date, k -> new java.util.HashSet<>())
                            .add(day.getCalendarId());
                }
            }
            // Повторяющиеся события
            var allEvents = db.eventDao().getByCalendarIds(calendarIds);
            for (var e : allEvents) {
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;
                var base = db.dayDao().getById(e.dayId);
                if (base == null) continue;
                LocalDate startDate = Instant.ofEpochMilli(base.getTimestamp())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                for (var date : daysInMonth) {
                    if (!activeDayCalendars.containsKey(date)
                            && EventUtils.occursOnDate(e, date, startDate)) {
                        activeDayCalendars
                                .computeIfAbsent(date, k -> new java.util.HashSet<>())
                                .add(e.calendarId);
                    }
                }
            }

            // 6. Загрузить награды
            Map<LocalDate, String> awardsMap = new HashMap<>();
            for (var day : days) {
                if (day.getAwardType() != null) {
                    LocalDate date = Instant.ofEpochMilli(day.getTimestamp())
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    awardsMap.put(date, day.getAwardType());
                }
            }

            // 7. Подсчитать стрик
            List<Integer> idsForStreak = (calendarId == -1)
                    ? new ArrayList<>(calendarIds)
                    : Collections.singletonList(calendarId);

            BiFunction<Integer, Calendar, Integer> calcStreak = (calId, startCal) -> {
                List<com.example.Kalendar.models.DayEntity> calDays =
                        db.dayDao().getByCalendarId(calId);
                calDays.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                int count = 0;
                Calendar pointer = (Calendar) startCal.clone();
                boolean cont = true;
                while (cont) {
                    boolean found = false;
                    for (var d : calDays) {
                        Calendar c0 = Calendar.getInstance();
                        c0.setTimeInMillis(d.getTimestamp());
                        c0.set(Calendar.HOUR_OF_DAY, 0);
                        c0.set(Calendar.MINUTE, 0);
                        c0.set(Calendar.SECOND, 0);
                        c0.set(Calendar.MILLISECOND, 0);
                        if (c0.getTimeInMillis() == pointer.getTimeInMillis()) {
                            found = true;
                            boolean allDone = db.taskDao().getTasksForDay(d.getId())
                                    .stream().allMatch(t -> t.done);
                            if (allDone) count++;
                            else cont = false;
                            break;
                        }
                    }
                    if (!found) cont = false;
                    pointer.add(Calendar.DAY_OF_YEAR, -1);
                }
                return count;
            };

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            List<Integer> streaks = new ArrayList<>();
            for (int id : idsForStreak) streaks.add(calcStreak.apply(id, today));

            int finalStreak = (calendarId == -1)
                    ? (streaks.isEmpty() ? 0 : Collections.min(streaks))
                    : streaks.get(0);

            live.postValue(new CalendarContent(
                    daysInMonth,
                    activeDayCalendars,
                    colorMap,
                    awardsMap,
                    spinnerCals,
                    calendarId,
                    finalStreak
            ));
        }).start();
        return live;
    }
}
