package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.TaskAdapter;
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.viewmodel.TasksViewModel;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksFragment extends Fragment {
    private static final String ARG_DATE = "date";

    public static TasksFragment newInstance(LocalDate date) {
        TasksFragment f = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date.toString());
        f.setArguments(args);
        return f;
    }

    private TasksViewModel vm;
    private TaskAdapter adapter;
    private Spinner spCategory;
    private CategorySpinnerAdapter catAdapter;
    private final List<CategoryEntity> categories = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_tasks, container, false);

        // RecyclerView
        RecyclerView rv = v.findViewById(R.id.tasksRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(requireContext(), new ArrayList<>(), this::onTasksChanged);
        rv.setAdapter(adapter);

        // Spinner категорий
        spCategory = v.findViewById(R.id.categoryFilter);
        catAdapter = new CategorySpinnerAdapter(
                requireContext(),
                categories,
                null,
                SessionManager.getLoggedInUserId(requireContext()),
                () -> catAdapter.notifyDataSetChanged()
        );
        spCategory.setAdapter(catAdapter);
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                vm.setCategory(parent.getItemAtPosition(pos).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ViewModel
        vm = new ViewModelProvider(this).get(TasksViewModel.class);
        LocalDate date = LocalDate.parse(getArguments().getString(ARG_DATE));
        vm.setDate(date);
        vm.setCalendarId(-1);
        vm.setCategory("Все");

        // Наблюдаем задачи
        vm.tasks.observe(getViewLifecycleOwner(), list -> adapter.setItems(list));

        return v;
    }

    private void onTasksChanged() {
        vm.setDate(vm.getDate());
    }

    public void refresh() {
        onTasksChanged();
    }
}