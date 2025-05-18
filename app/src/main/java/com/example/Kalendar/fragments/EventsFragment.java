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
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventsFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private EventAdapter adapter;
    private final List<EventEntity> events = new ArrayList<>();

    private Spinner filterSpinner;
    private int currentUserId;
    private List<Integer> myCalendarIds;

    public static EventsFragment newInstance(LocalDate date) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        date = LocalDate.parse(requireArguments().getString("date"));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        RecyclerView rv = view.findViewById(R.id.eventsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(requireContext(), events);
        rv.setAdapter(adapter);

        // Настройка фильтра
        filterSpinner = view.findViewById(R.id.categoryFilter);
        List<String> filters = new ArrayList<>();
        filters.add("Все");
        filters.add("Без категории");
        new Thread(() -> {
            List<com.example.Kalendar.models.CategoryEntity> cats =
                    db.categoryDao().getAllForUser(currentUserId);
            for (com.example.Kalendar.models.CategoryEntity c : cats) {
                if (!"Без категории".equals(c.name)) filters.add(c.name);
            }
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> fa = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        filters
                );
                filterSpinner.setAdapter(fa);
                filterSpinner.setSelection(0);
            });
        }).start();

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                loadEventsForDate();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Загрузка календарей и первый показ
        new Thread(() -> {
            List<CalendarEntity> myCals = db.calendarDao().getByUserId(currentUserId);
            myCalendarIds = myCals.stream().map(c -> c.id).collect(Collectors.toList());
            loadEventsForDate();
        }).start();

        return view;
    }

    private void loadEventsForDate() {
        String sel = (String) filterSpinner.getSelectedItem();
        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<DayEntity> days = db.dayDao().getByTimestamp(ts).stream()
                    .filter(d -> myCalendarIds.contains(d.calendarId))
                    .collect(Collectors.toList());

            List<EventEntity> allEvents = new ArrayList<>();
            Set<Integer> seen = new HashSet<>();

            for (DayEntity d : days) {
                for (EventEntity e : db.eventDao().getEventsForDay(d.id)) {
                    boolean isOriginal = d.id == e.dayId;
                    LocalDate start = Instant.ofEpochMilli(d.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    if (e.repeatRule != null && !e.repeatRule.isEmpty()) {
                        if (EventUtils.occursOnDate(e, date, start)) {
                            EventEntity ev = isOriginal ? e : createVirtualCopy(e, d);
                            allEvents.add(ev);
                            seen.add(e.id);
                        }
                    } else if (isOriginal) {
                        allEvents.add(e);
                        seen.add(e.id);
                    }
                }
            }

            for (EventEntity e : db.eventDao().getAll()) {
                if (myCalendarIds.contains(e.calendarId)
                        && e.repeatRule != null && !e.repeatRule.isEmpty()
                        && !seen.contains(e.id)) {
                    DayEntity base = db.dayDao().getById(e.dayId);
                    if (base != null) {
                        LocalDate start = Instant.ofEpochMilli(base.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        if (EventUtils.occursOnDate(e, date, start)) {
                            allEvents.add(createVirtualCopy(e, base));
                        }
                    }
                }
            }

            List<EventEntity> filtered;
            if ("Все".equals(sel)) {
                filtered = allEvents;
            } else if ("Без категории".equals(sel)) {
                filtered = allEvents.stream()
                        .filter(ev -> ev.category == null || ev.category.trim().isEmpty() || "Без категории".equals(ev.category))
                        .collect(Collectors.toList());
            } else {
                filtered = allEvents.stream()
                        .filter(ev -> sel.equals(ev.category))
                        .collect(Collectors.toList());
            }

            requireActivity().runOnUiThread(() -> adapter.updateEvents(filtered));
        }).start();
    }

    public LocalDate getDate() {
        return date;
    }
    private EventEntity createVirtualCopy(EventEntity e, DayEntity d) {
        EventEntity copy = new EventEntity();
        copy.title               = e.title;
        copy.timeStart           = e.timeStart;
        copy.timeEnd             = e.timeEnd;
        copy.allDay              = e.allDay;
        copy.repeatRule          = e.repeatRule;
        copy.excludedDates       = e.excludedDates;
        copy.category            = e.category;
        copy.location            = e.location;
        copy.description         = e.description;
        copy.done                = false;
        copy.dayId               = d.id;
        copy.calendarId          = e.calendarId;
        copy.notifyOnStart       = e.notifyOnStart;
        copy.earlyReminderEnabled= e.earlyReminderEnabled;
        copy.earlyReminderHour   = e.earlyReminderHour;
        copy.earlyReminderMinute = e.earlyReminderMinute;
        copy.date                = date.toString();
        return copy;
    }

    public void refresh() {
        loadEventsForDate();
    }
}
