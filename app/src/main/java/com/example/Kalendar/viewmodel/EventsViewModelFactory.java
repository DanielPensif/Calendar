package com.example.Kalendar.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.Kalendar.domain.LoadEventsUseCase;
import com.example.Kalendar.repository.EventRepository;

import javax.inject.Inject;

public class EventsViewModelFactory implements ViewModelProvider.Factory {
    private final LoadEventsUseCase useCase;

    @Inject
    public EventsViewModelFactory(LoadEventsUseCase useCase) {
        this.useCase = useCase;
    }

    @SuppressWarnings("unchecked")
    @NonNull @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EventsViewModel.class)) {
            return (T) new EventsViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass);
    }
}