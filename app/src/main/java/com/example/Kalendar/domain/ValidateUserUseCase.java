package com.example.Kalendar.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;
import com.example.Kalendar.utils.PasswordUtils;
import dagger.hilt.android.scopes.ViewModelScoped;
import javax.inject.Inject;

@ViewModelScoped
public class ValidateUserUseCase {
    private final UserRepository repo;

    @Inject
    public ValidateUserUseCase(UserRepository repo) {
        this.repo = repo;
    }
    public LiveData<UserEntity> execute(String username, String password) {
        LiveData<UserEntity> source = repo.getUserByUsername(username);
        return Transformations.map(source, user -> {
            if (user != null && PasswordUtils.hash(password).equals(user.passwordHash)) {
                return user;
            }
            return null;
        });
    }
}