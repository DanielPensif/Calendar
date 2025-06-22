package com.example.Kalendar.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.Kalendar.domain.LoadTasksUseCase;
import com.example.Kalendar.repository.TaskRepository;

import javax.inject.Inject;


public class TasksViewModelFactory implements ViewModelProvider.Factory {
    private final LoadTasksUseCase useCase;

    @Inject
    public TasksViewModelFactory(LoadTasksUseCase useCase) {
        this.useCase = useCase;
    }

    @SuppressWarnings("unchecked")
    @NonNull @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TasksViewModel.class)) {
            return (T) new TasksViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass);
    }
}