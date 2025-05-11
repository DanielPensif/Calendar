// EventsFragment.java
package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.EventAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;
import com.example.Kalendar.adapters.SessionManager;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;
import java.util.stream.Collectors;

public class EventsFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private EventAdapter adapter;
    private final List<EventEntity> events = new ArrayList<>();

    private final Set<String> selectedCategories = new HashSet<>();
    private final List<String> allCategories = Arrays.asList("Все","Работа","Встреча","Учёба","Личное");

    private int currentUserId;
    private List<Integer> myCalendarIds = Collections.emptyList();

    public static EventsFragment newInstance(LocalDate date) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public LocalDate getDate() {
        return date;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        RecyclerView rv = view.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(requireContext(), events);
        rv.setAdapter(adapter);

        Spinner filterSpinner = view.findViewById(R.id.categoryFilter);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                allCategories
        );
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setSelection(0);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                selectedCategories.clear();
                if (pos == 0) {
                    selectedCategories.addAll(allCategories.subList(1, allCategories.size()));
                } else {
                    selectedCategories.add(allCategories.get(pos));
                }
                loadEventsForDate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // загрузка моих календарей и первого раза событий
        new Thread(() -> {
            List<CalendarEntity> myCals = db.calendarDao().getByUserId(currentUserId);
            myCalendarIds = myCals.stream()
                    .map(c -> c.id)
                    .collect(Collectors.toList());
            // инициализируем фильтр "Все"
            selectedCategories.clear();
            selectedCategories.addAll(allCategories.subList(1, allCategories.size()));
            loadEventsForDate();
        }).start();

        return view;
    }

    private void loadEventsForDate() {
        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            // 1) дни моего пользователя
            List<DayEntity> days = db.dayDao().getByTimestamp(ts).stream()
                    .filter(d -> myCalendarIds.contains(d.calendarId))
                    .collect(Collectors.toList());

            List<EventEntity> allEvents = new ArrayList<>();
            Set<Integer> accounted = new HashSet<>();

            // 2) оригинальные и виртуальные экземпляры
            for (DayEntity d : days) {
                List<EventEntity> evs = db.eventDao().getEventsForDay(d.id);
                for (EventEntity e : evs) {
                    boolean isOriginal = d.id == e.dayId;
                    LocalDate start = Instant.ofEpochMilli(d.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    if (e.repeatRule != null && !e.repeatRule.isEmpty()) {
                        if (EventUtils.occursOnDate(e, date, start)) {
                            if (!isOriginal) {
                                allEvents.add(createVirtualCopy(e, d));
                            } else {
                                allEvents.add(e);
                            }
                            accounted.add(e.id);
                        }
                    } else if (isOriginal) {
                        allEvents.add(e);
                        accounted.add(e.id);
                    }
                }
            }

            // 3) доп. виртуальные повторы из всех событий, но только свои календари
            db.eventDao().getAll().stream()
                    .filter(e -> myCalendarIds.contains(e.calendarId))
                    .filter(e -> e.repeatRule != null && !e.repeatRule.isEmpty())
                    .filter(e -> !accounted.contains(e.id))
                    .forEach(e -> {
                        DayEntity base = db.dayDao().getById(e.dayId);
                        if (base != null) {
                            LocalDate start = Instant.ofEpochMilli(base.timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            if (EventUtils.occursOnDate(e, date, start)) {
                                DayEntity vday = new DayEntity();
                                vday.id = base.id;
                                vday.calendarId = e.calendarId;
                                vday.timestamp = ts;
                                allEvents.add(createVirtualCopy(e, vday));
                            }
                        }
                    });

            // 4) фильтрация и сортировка
            List<EventEntity> filtered = allEvents.stream()
                    .filter(e -> selectedCategories.contains(e.category))
                    .sorted((a, b) -> {
                        if (a.allDay && !b.allDay) return -1;
                        if (!a.allDay && b.allDay) return 1;
                        int t1 = safeToMinutes(a.timeStart);
                        int t2 = safeToMinutes(b.timeStart);
                        return Integer.compare(t1, t2);
                    })
                    .collect(Collectors.toList());

            requireActivity().runOnUiThread(() -> adapter.updateEvents(filtered));
        }).start();
    }

    private EventEntity createVirtualCopy(EventEntity e, DayEntity day) {
        EventEntity copy = new EventEntity();
        copy.title         = e.title;
        copy.timeStart     = e.timeStart;
        copy.timeEnd       = e.timeEnd;
        copy.allDay        = e.allDay;
        copy.repeatRule    = e.repeatRule;
        copy.excludedDates = e.excludedDates;
        copy.category      = e.category;
        copy.location      = e.location;
        copy.description   = e.description;
        copy.done          = false;
        copy.dayId         = day.id;
        copy.calendarId    = e.calendarId;
        copy.date          = date.toString();
        return copy;
    }

    private int safeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60
                    + Integer.parseInt(parts[1]);
        } catch (Exception ex) {
            return 0;
        }
    }

    public void refresh() {
        loadEventsForDate();
    }
}
