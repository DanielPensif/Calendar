package com.example.Kalendar.domain;

import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;

import javax.inject.Inject;
import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class UpdateUserUseCase {
    private final UserRepository repo;

    @Inject
    public UpdateUserUseCase(UserRepository repo) {
        this.repo = repo;
    }

    public void execute(UserEntity user) {
        repo.update(user);
    }
}