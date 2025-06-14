package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;

import javax.inject.Inject;
import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class GetUserUseCase {
    private final UserRepository repo;

    @Inject
    public GetUserUseCase(UserRepository repo) {
        this.repo = repo;
    }

    public LiveData<UserEntity> execute(int userId) {
        return repo.getUser(userId);
    }
}
