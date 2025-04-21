package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.EventAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventsFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private EventAdapter adapter;
    private final List<EventEntity> events = new ArrayList<>();

    private final Set<String> selectedCategories = new HashSet<>();

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));
        db = AppDatabase.getDatabase(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.eventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(events);
        recyclerView.setAdapter(adapter);

        Spinner filterSpinner = view.findViewById(R.id.categoryFilter);
        List<String> allCategories = Arrays.asList("Все", "Работа", "Встреча", "Учёба", "Личное");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, allCategories);
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategories.clear();
                if (position == 0) {
                    selectedCategories.addAll(allCategories.subList(1, allCategories.size()));
                } else {
                    selectedCategories.add(allCategories.get(position));
                }
                loadEventsForDate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        loadEventsForDate();

        return view;
    }

    private void loadEventsForDate() {
        new Thread(() -> {
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<DayEntity> days = db.dayDao().getByTimestamp(timestamp);
            List<EventEntity> allEvents = new ArrayList<>();

            Set<Integer> accountedIds = new HashSet<>();

            if (!days.isEmpty()) {
                for (DayEntity d : days) {
                    List<EventEntity> events = db.eventDao().getEventsForDay(d.id);

                    for (EventEntity e : events) {
                        boolean isOriginal = d.id == e.dayId;
                        LocalDate start = Instant.ofEpochMilli(d.timestamp)
                                .atZone(ZoneId.systemDefault()).toLocalDate();

                        if (e.repeatRule != null && !e.repeatRule.isEmpty()) {
                            if (EventUtils.occursOnDate(e, date, start)) {
                                if (!isOriginal) {
                                    allEvents.add(createVirtualCopy(e, d));
                                } else {
                                    allEvents.add(e);
                                }
                                accountedIds.add(e.id);
                            }
                        } else if (isOriginal) {
                            allEvents.add(e);
                            accountedIds.add(e.id);
                        }
                    }
                }
            }

            // Обработка повторов, не добавленных ранее
            List<EventEntity> all = db.eventDao().getAll();
            for (EventEntity e : all) {
                if (accountedIds.contains(e.id)) continue;
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;

                DayEntity base = db.dayDao().getById(e.dayId);
                if (base == null) continue;

                LocalDate start = Instant.ofEpochMilli(base.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (EventUtils.occursOnDate(e, date, start)) {
                    DayEntity virtualDay = new DayEntity();
                    virtualDay.id = base.id;
                    virtualDay.calendarId = e.calendarId;
                    virtualDay.timestamp = timestamp;

                    allEvents.add(createVirtualCopy(e, virtualDay));
                }
            }

            // фильтрация и сортировка
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

            requireActivity().runOnUiThread(() -> {
                adapter.updateEvents(filtered);
            });
        }).start();
    }


    private EventEntity createVirtualCopy(EventEntity e, DayEntity day) {
        EventEntity copy = new EventEntity();
        copy.title = e.title;
        copy.timeStart = e.timeStart;
        copy.timeEnd = e.timeEnd;
        copy.allDay = e.allDay;
        copy.repeatRule = e.repeatRule;
        copy.excludedDates = e.excludedDates;
        copy.category = e.category;
        copy.description = e.description;
        copy.location = e.location;
        copy.done = false;
        copy.dayId = day.id;
        copy.calendarId = e.calendarId;
        copy.date = date.toString();
        return copy;
    }





    private int safeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }



    public void refresh() {
        loadEventsForDate();
    }

}

