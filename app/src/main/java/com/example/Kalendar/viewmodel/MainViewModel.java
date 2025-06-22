package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.InitTodayUseCase;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final InitTodayUseCase initTodayUseCase;
    private final Executor ioExecutor;
    private final MutableLiveData<Boolean> initComplete = new MutableLiveData<>();

    @Inject
    public MainViewModel(InitTodayUseCase uc, Executor ioExecutor) {
        this.initTodayUseCase = uc;
        this.ioExecutor       = ioExecutor;
    }

    public LiveData<Boolean> getInitComplete() {
        return initComplete;
    }

    /** ВСЯ тяжёлая работа уходит в фоновый поток */
    public void initForUser(int userId) {
        ioExecutor.execute(() -> {
            initTodayUseCase.execute(userId);
            initComplete.postValue(true);
        });
    }
}
