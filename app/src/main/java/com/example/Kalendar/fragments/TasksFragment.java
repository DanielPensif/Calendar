package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
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
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.TaskAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TasksFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private TaskAdapter adapter;
    private final List<TaskEntity> tasks = new ArrayList<>();

    private Spinner filterSpinner;
    private Spinner spinnerCategory;
    private CategorySpinnerAdapter categoryAdapter;
    private final List<com.example.Kalendar.models.CategoryEntity> categories = new ArrayList<>();

    private int currentUserId;
    private List<Integer> myCalendarIds;

    public static TasksFragment newInstance(LocalDate date) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        date = LocalDate.parse(requireArguments().getString("date"));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        RecyclerView rv = view.findViewById(R.id.tasksRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), tasks, this::loadTasksForDate);
        rv.setAdapter(adapter);

        // filter spinner setup
        filterSpinner = view.findViewById(R.id.categoryFilter);
        List<String> filters = new ArrayList<>();
        filters.add("Все");
        filters.add("Без категории");
        new Thread(() -> {
            List<com.example.Kalendar.models.CategoryEntity> cats = db.categoryDao().getAllForUser(currentUserId);
            for (com.example.Kalendar.models.CategoryEntity c : cats) {
                if (!"Без категории".equals(c.name)) filters.add(c.name);
            }
            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> fa = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, filters);
                filterSpinner.setAdapter(fa);
                filterSpinner.setSelection(0);
            });
        }).start();

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                loadTasksForDate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // category spinner for save dialogs can be elsewhere

        new Thread(() -> {
            List<CalendarEntity> myCals = db.calendarDao().getByUserId(currentUserId);
            myCalendarIds = myCals.stream().map(c -> c.id).collect(Collectors.toList());
            loadTasksForDate();
        }).start();

        return view;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksForDate() {
        String sel = (String) filterSpinner.getSelectedItem();
        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<DayEntity> days = db.dayDao().getByTimestamp(ts).stream()
                    .filter(d -> myCalendarIds.contains(d.calendarId)).collect(Collectors.toList());

            List<TaskEntity> all = new ArrayList<>();
            for (DayEntity d : days) all.addAll(db.taskDao().getTasksForDay(d.id));

            List<TaskEntity> filtered;
            if ("Все".equals(sel)) {
                filtered = all;
            } else if ("Без категории".equals(sel)) {
                filtered = all.stream()
                        .filter(t -> t.category == null || t.category.trim().isEmpty() || "Без категории".equals(t.category))
                        .collect(Collectors.toList());
            } else {
                filtered = all.stream().filter(t -> sel.equals(t.category)).collect(Collectors.toList());
            }

            List<TaskEntity> active = filtered.stream().filter(t -> !t.done).collect(Collectors.toList());
            List<TaskEntity> done   = filtered.stream().filter(t -> t.done).collect(Collectors.toList());

            requireActivity().runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(active);
                if (!done.isEmpty()) {
                    tasks.add(null);
                    tasks.addAll(done);
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }


    public void refresh() {
        loadTasksForDate();
    }
}
