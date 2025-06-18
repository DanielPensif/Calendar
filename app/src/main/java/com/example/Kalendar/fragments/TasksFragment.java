package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.TaskAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.viewmodel.TasksViewModel;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {
    private static final String ARG_DATE = "date";

    public static TasksFragment newInstance(LocalDate date) {
        TasksFragment f = new TasksFragment();
        Bundle b = new Bundle();
        b.putString(ARG_DATE, date.toString());
        f.setArguments(b);
        return f;
    }

    private TasksViewModel vm;
    private TaskAdapter adapter;
    private Spinner spCategory;
    private CategorySpinnerAdapter catAdapter;
    private final List<CategoryEntity> categories = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup ct, @Nullable Bundle bs) {
        View v = inf.inflate(R.layout.fragment_tasks, ct, false);

        // RecyclerView
        RecyclerView rv = v.findViewById(R.id.tasksRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), new ArrayList<>(), this::onTasksChanged);
        rv.setAdapter(adapter);

        // Spinner категорий
        spCategory = v.findViewById(R.id.categoryFilter);
        catAdapter = new CategorySpinnerAdapter(
                requireContext(), categories,
                null, // передаём null в db, потому что CategorySpinnerAdapter перезагрузит сама
                SessionManager.getLoggedInUserId(requireContext()),
                () -> catAdapter.notifyDataSetChanged()
        );
        spCategory.setAdapter(catAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View w, int pos, long id) {
                vm.setCategory(p.getItemAtPosition(pos).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> p){}
        });

        // ViewModel
        vm = new ViewModelProvider(this).get(TasksViewModel.class);
        LocalDate date = LocalDate.parse(getArguments().getString(ARG_DATE));
        vm.setDate(date);
        vm.setCalendarId(-1);
        vm.setCategory("Все");

        // наблюдаем
        vm.tasks.observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
        });

        return v;
    }

    private void onTasksChanged() {
        // просто перезапрос
        vm.setDate(vm.getDate());
    }

    public void refresh() {
        onTasksChanged();
    }
}