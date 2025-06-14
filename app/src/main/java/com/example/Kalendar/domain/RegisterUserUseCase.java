package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;
import dagger.hilt.android.scopes.ViewModelScoped;
import javax.inject.Inject;

@ViewModelScoped
public class RegisterUserUseCase {
    private final UserRepository repo;

    @Inject
    public RegisterUserUseCase(UserRepository repo) {
        this.repo = repo;
    }
    public LiveData<Boolean> execute(UserEntity user) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repo.insert(user);
        result.postValue(true);
        return result;
    }
}
