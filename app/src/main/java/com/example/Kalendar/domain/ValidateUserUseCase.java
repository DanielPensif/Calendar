package com.example.Kalendar.domain;

import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class ValidateUserUseCase {
    private final UserRepository userRepo;

    @Inject
    public ValidateUserUseCase(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserEntity execute(String username, String passwordHash) {
        UserEntity user = userRepo.getByUsernameSync(username);
        if (user != null && passwordHash.equals(user.getPasswordHash())) {
            return user;
        }
        return null;
    }
}
