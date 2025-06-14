package com.example.Kalendar.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.InitTodayUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final InitTodayUseCase initUseCase;

    @Inject
    public MainViewModel(InitTodayUseCase uc) { this.initUseCase = uc; }

    public void initToday(int userId) {
        new Thread(() -> initUseCase.execute(userId)).start();
    }
}
