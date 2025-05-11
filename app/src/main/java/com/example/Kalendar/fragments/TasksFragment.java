// TasksFragment.java
package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
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
import com.example.Kalendar.adapters.TaskAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.adapters.SessionManager;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;
import java.util.stream.Collectors;

public class TasksFragment extends Fragment {

    private LocalDate date;
    private AppDatabase db;
    private TaskAdapter adapter;
    private final List<TaskEntity> tasks = new ArrayList<>();

    private final Set<String> selectedCategories = new HashSet<>();
    private final List<String> allCategories = Arrays.asList("Все", "Учёба", "Работа", "Быт", "Личное");

    private int currentUserId;
    private List<Integer> myCalendarIds = Collections.emptyList();

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

        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        // 1) RecyclerView + Adapter
        RecyclerView recyclerView = view.findViewById(R.id.tasksRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), tasks, this::loadTasksForDate);
        recyclerView.setAdapter(adapter);

        // 2) Spinner фильтра категорий
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
                loadTasksForDate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3) Загрузка моих календарей, потом задач
        new Thread(() -> {
            List<CalendarEntity> myCals = db.calendarDao().getByUserId(currentUserId);
            myCalendarIds = myCals.stream()
                    .map(c -> c.id)
                    .collect(Collectors.toList());
            // инициализируем фильтр "Все"
            selectedCategories.clear();
            selectedCategories.addAll(allCategories.subList(1, allCategories.size()));
            loadTasksForDate();
        }).start();

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksForDate() {
        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            // получаем все дни в этот день
            List<DayEntity> days = db.dayDao().getByTimestamp(ts).stream()
                    .filter(d -> myCalendarIds.contains(d.calendarId))
                    .collect(Collectors.toList());

            // собираем все задачи из этих дней
            List<TaskEntity> all = new ArrayList<>();
            for (DayEntity d : days) {
                all.addAll(db.taskDao().getTasksForDay(d.id));
            }

            // фильтрация и сортировка
            List<TaskEntity> filtered = all.stream()
                    .filter(t -> selectedCategories.contains(t.category))
                    .sorted((a, b) -> {
                        if (a.done != b.done) return Boolean.compare(a.done, b.done);
                        return a.title.compareToIgnoreCase(b.title);
                    })
                    .collect(Collectors.toList());

            // разделяем на активные и выполненные
            List<TaskEntity> active = filtered.stream().filter(t -> !t.done).collect(Collectors.toList());
            List<TaskEntity> done   = filtered.stream().filter(t -> t.done).collect(Collectors.toList());

            List<TaskEntity> display = new ArrayList<>();
            display.addAll(active);
            if (!done.isEmpty()) {
                display.add(null); // header
                display.addAll(done);
            }

            requireActivity().runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(display);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void refresh() {
        loadTasksForDate();
    }
}
