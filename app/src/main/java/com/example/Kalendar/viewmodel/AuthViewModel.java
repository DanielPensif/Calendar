package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.RegisterUserUseCase;
import com.example.Kalendar.domain.ValidateUserUseCase;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.utils.PasswordUtils;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final ValidateUserUseCase validateUseCase;
    private final RegisterUserUseCase registerUseCase;
    private final Executor ioExecutor;

    private final MutableLiveData<UserEntity> _loginResult   = new MutableLiveData<>();
    public  final LiveData<UserEntity>     loginResult      = _loginResult;

    private final MutableLiveData<Boolean> _registerResult = new MutableLiveData<>();
    public  final LiveData<Boolean>        registerResult  = _registerResult;

    private final MutableLiveData<String>  _error          = new MutableLiveData<>();
    public  final LiveData<String>         error           = _error;

    @Inject
    public AuthViewModel(
            ValidateUserUseCase validateUseCase,
            RegisterUserUseCase registerUseCase,
            Executor ioExecutor
    ) {
        this.validateUseCase = validateUseCase;
        this.registerUseCase = registerUseCase;
        this.ioExecutor      = ioExecutor;
    }

    public void login(String username, String password) {
        ioExecutor.execute(() -> {
            if (username == null || username.isEmpty() ||
                    password == null || password.isEmpty()) {
                _error.postValue("Введите логин и пароль");
                return;
            }

            String hash = PasswordUtils.hash(password);
            UserEntity user = validateUseCase.execute(username, hash);

            if (user != null) {
                _loginResult.postValue(user);
            } else {
                _error.postValue("Неверный логин или пароль");
            }
        });
    }

    public void register(String username, String password, String repeatPassword) {
        ioExecutor.execute(() -> {
            if (username == null || username.isEmpty() ||
                    password == null || password.isEmpty() ||
                    repeatPassword == null || repeatPassword.isEmpty()) {
                _error.postValue("Заполните все поля");
                return;
            }
            if (!password.equals(repeatPassword)) {
                _error.postValue("Пароли не совпадают");
                return;
            }

            UserEntity newUser = new UserEntity();
            newUser.setUsername(username);
            newUser.setPasswordHash(PasswordUtils.hash(password));

            boolean success = registerUseCase.execute(newUser);
            if (success) {
                _registerResult.postValue(true);
            } else {
                _error.postValue("Пользователь с таким логином уже существует");
            }
        });
    }
}
