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
            // 1. Загрузить все календари пользователя
            List<com.example.Kalendar.models.CalendarEntity> allCals =
                    calRepo.getByUserIdSync(userId);

            // 2. Построить colorMap
            Map<Integer,String> colorMap = new HashMap<>();
            for (var c : allCals) colorMap.put(c.id, c.colorHex);

            // 3. Сформировать список дней месяца (42 или 35)
            List<LocalDate> daysInMonth = new ArrayList<>();
            LocalDate firstOfMonth = monthStart.withDayOfMonth(1);
            int shift = firstOfMonth.getDayOfWeek().getValue() - 1; // 1=понедельник
            LocalDate start = firstOfMonth.minusDays(shift);
            for (int i=0;i<42;i++) daysInMonth.add(start.plusDays(i));
            // обрезать последнюю неделю, если полностью вне месяца
            if (daysInMonth.subList(35,42).stream()
                    .allMatch(d -> d.getMonthValue()!=monthStart.getMonthValue()))
                daysInMonth = daysInMonth.subList(0,35);

            // 4. Загрузить DayEntity
            List<Integer> calendarIds;
            if (calendarId == -1) {
                List<CalendarEntity> cals = calRepo.getByUserIdSync(userId);
                calendarIds = new ArrayList<>(cals.size());
                for (CalendarEntity c : cals) {
                    calendarIds.add(c.id);
                }
            } else {
                calendarIds = Collections.singletonList(calendarId);
            }
            List<com.example.Kalendar.models.DayEntity> days =
                    dayRepo.getByCalendarIdsSync(calendarIds);

            // 5. Построить activeDayCalendars
            Map<LocalDate,Set<Integer>> activeDayCalendars = new HashMap<>();
            for (var day : days) {
                LocalDate date = Instant.ofEpochMilli(day.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                boolean hasTask = !db.taskDao().getTasksForDay(day.id).isEmpty();
                boolean hasEvent = db.eventDao().getEventsForDay(day.id).stream().anyMatch(e ->
                        (e.repeatRule==null||e.repeatRule.isEmpty()) ||
                                EventUtils.occursOnDate(e,date,
                                        Instant.ofEpochMilli(day.timestamp)
                                                .atZone(ZoneId.systemDefault()).toLocalDate())
                );
                if (hasTask||hasEvent) {
                    activeDayCalendars
                            .computeIfAbsent(date, k->new HashSet<>())
                            .add(day.calendarId);
                }
            }
            // плюс репиты для невидимых дней
            var allEvents = db.eventDao().getByCalendarIds(calendarIds);
            for (var e : allEvents) {
                if (e.repeatRule==null||e.repeatRule.isEmpty()) continue;
                var base = db.dayDao().getById(e.dayId);
                if (base==null) continue;
                LocalDate startDate = Instant.ofEpochMilli(base.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                for (var date : daysInMonth) {
                    if (!activeDayCalendars.containsKey(date)
                            && EventUtils.occursOnDate(e,date,startDate)) {
                        activeDayCalendars
                                .computeIfAbsent(date, k->new HashSet<>())
                                .add(e.calendarId);
                    }
                }
            }

            // 6. Загрузить награды
            Map<LocalDate,String> awardsMap = new HashMap<>();
            for (var day : days) {
                if (day.awardType!=null) {
                    LocalDate date = Instant.ofEpochMilli(day.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    awardsMap.put(date, day.awardType);
                }
            }

            // 7. Подсчитать стрик
            int streak=0;
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY,0);
            today.set(Calendar.MINUTE,0);
            today.set(Calendar.SECOND,0);
            today.set(Calendar.MILLISECOND,0);
            List<com.example.Kalendar.models.DayEntity> calDays =
                    db.dayDao().getByCalendarId(calendarId==-1?allCals.get(0).id:calendarId);
            calDays.sort((a,b)->Long.compare(b.timestamp,a.timestamp));
            boolean cont=true;
            while(cont) {
                boolean found=false;
                for(var d:calDays) {
                    Calendar c=Calendar.getInstance();
                    c.setTimeInMillis(d.timestamp);
                    c.set(Calendar.HOUR_OF_DAY,0);
                    c.set(Calendar.MINUTE,0);
                    c.set(Calendar.SECOND,0);
                    c.set(Calendar.MILLISECOND,0);
                    if (c.getTimeInMillis()==today.getTimeInMillis()) {
                        found=true;
                        boolean allDone = db.taskDao().getTasksForDay(d.id)
                                .stream().allMatch(t->t.done);
                        if (allDone) streak++;
                        else cont=false;
                        break;
                    }
                }
                if (!found) cont=false;
                today.add(Calendar.DAY_OF_YEAR,-1);
            }

            live.postValue(new CalendarContent(
                    daysInMonth,
                    activeDayCalendars,
                    colorMap,
                    awardsMap,
                    allCals,
                    calendarId,
                    streak
            ));
        }).start();
        return live;
    }
}