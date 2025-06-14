package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.domain.ValidateUserUseCase;
import com.example.Kalendar.domain.RegisterUserUseCase;
import com.example.Kalendar.utils.PasswordUtils;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final ValidateUserUseCase validateUseCase;
    private final RegisterUserUseCase registerUseCase;

    private final MutableLiveData<LoginParams> loginParams = new MutableLiveData<>();
    public final LiveData<UserEntity> loginResult;

    private final MutableLiveData<RegisterParams> registerParams = new MutableLiveData<>();
    public final LiveData<Boolean> registerResult;

    public final MediatorLiveData<String> error = new MediatorLiveData<>();

    @Inject
    public AuthViewModel(ValidateUserUseCase v, RegisterUserUseCase r) {
        this.validateUseCase = v;
        this.registerUseCase = r;

        loginResult = Transformations.switchMap(loginParams, p -> {
            if (p.login.isEmpty() || p.password.isEmpty()) {
                error.postValue("Введите логин и пароль");
                return new MutableLiveData<>();
            }
            return validateUseCase.execute(p.login, PasswordUtils.hash(p.password));
        });

        registerResult = Transformations.switchMap(registerParams, p -> {
            if (p.login.isEmpty() || p.password.isEmpty() || p.repeatPassword.isEmpty()) {
                error.postValue("Заполните все поля");
                return new MutableLiveData<>();
            }
            if (!p.password.equals(p.repeatPassword)) {
                error.postValue("Пароли не совпадают");
                return new MutableLiveData<>();
            }
            UserEntity u = new UserEntity();
            u.username = p.login;
            u.passwordHash = PasswordUtils.hash(p.password);
            return registerUseCase.execute(u);
        });
    }

    public void login(String login, String password) {
        loginParams.postValue(new LoginParams(login, password));
    }

    public void register(String login, String password, String repeatPassword) {
        registerParams.postValue(new RegisterParams(login, password, repeatPassword));
    }

    private static class LoginParams { final String login, password; LoginParams(String l, String p) { login = l; password = p; }}
    private static class RegisterParams { final String login, password, repeatPassword; RegisterParams(String l, String p, String r) { login = l; password = p; repeatPassword = r; }}
}