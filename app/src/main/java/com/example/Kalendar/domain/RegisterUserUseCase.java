package com.example.Kalendar.domain;

import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class RegisterUserUseCase {
    private final UserRepository userRepo;

    @Inject
    public RegisterUserUseCase(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public boolean execute(UserEntity newUser) {
        // 1) Не даём зарегистрировать дубль
        if (userRepo.getByUsernameSync(newUser.getUsername()) != null) {
            return false;
        }

        // 2) Вставляем и возвращаем результат
        try {
            long rowId = userRepo.insertSync(newUser);
            return rowId > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
