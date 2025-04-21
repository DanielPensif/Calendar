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
import com.example.Kalendar.adapters.TaskAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TasksFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private TaskAdapter adapter;
    private final List<TaskEntity> tasks = new ArrayList<>();

    private final Set<String> selectedCategories = new HashSet<>();


    public static TasksFragment newInstance(LocalDate date) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));
        db = AppDatabase.getDatabase(requireContext());
        RecyclerView recyclerView = view.findViewById(R.id.tasksRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(tasks, null);
        recyclerView.setAdapter(adapter);

        Spinner filterSpinner = view.findViewById(R.id.categoryFilter);
        List<String> allCategories = Arrays.asList("Все", "Учёба", "Работа", "Быт", "Личное");
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
                loadTasksForDate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksForDate() {
        new Thread(() -> {
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            List<DayEntity> days = db.dayDao().getByTimestamp(timestamp);
            List<TaskEntity> allTasks = new ArrayList<>();
            List<TaskEntity> filtered = new ArrayList<>();

            for (DayEntity d : days) {
                allTasks.addAll(db.taskDao().getTasksForDay(d.id));
            }

            allTasks.sort((a, b) -> {
                if (a.done != b.done) return Boolean.compare(a.done, b.done);
                return a.title.compareToIgnoreCase(b.title);
            });

            for (TaskEntity t : allTasks) {
                if (selectedCategories.contains(t.category)) {
                    filtered.add(t);
                }
            }

            List<TaskEntity> active = new ArrayList<>();
            List<TaskEntity> done = new ArrayList<>();

            for (TaskEntity t : allTasks) {
                if (selectedCategories.contains(t.category)) {
                    if (t.done) done.add(t);
                    else active.add(t);
                }
            }

            tasks.clear();
            tasks.addAll(active);
            if (!done.isEmpty()) {
                tasks.add(null);
                tasks.addAll(done);
            }

            requireActivity().runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(filtered);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void refresh() {
        loadTasksForDate();
    }


}


