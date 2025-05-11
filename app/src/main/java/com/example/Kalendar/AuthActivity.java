package com.example.Kalendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.fragments.LoginFragment;
import com.example.Kalendar.fragments.RegisterFragment;
import com.example.Kalendar.utils.BiometricHelper;

public class AuthActivity extends AppCompatActivity
        implements LoginFragment.OnAuthSuccessListener,
        RegisterFragment.OnRegisterSuccessListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        int userId = SessionManager.getLoggedInUserId(this);
        if (userId != -1) {
            // Пользователь уже вошёл
            if (!SessionManager.isBiometricAsked(this)) {
                // спрашиваем впервые
                askUseBiometric();
            } else if (SessionManager.isBiometricEnabled(this)) {
                // спросили и включили → биометрия
                promptBiometricAndEnter();
            } else {
                // спросили и выключили → сразу в приложение
                startMain();
            }
        } else {
            // ещё не входил → показываем Login
            showLogin();
        }
    }

    // Показываем LoginFragment
    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, new LoginFragment())
                .commit();
    }

    // Спрашиваем, нужен ли биометрический вход
    private void askUseBiometric() {
        new AlertDialog.Builder(this)
                .setTitle("Включить вход по биометрии?")
                .setMessage("Хотите использовать отпечаток или код устройства для скрытия данных?")
                .setPositiveButton("Да", (dialog, which) -> {
                    SessionManager.setBiometricEnabled(this, true);
                    SessionManager.setBiometricAsked(this, true);
                    promptBiometricAndEnter();
                })
                .setNegativeButton("Нет", (dialog, which) -> {
                    SessionManager.setBiometricEnabled(this, false);
                    SessionManager.setBiometricAsked(this, true);
                    startMain();
                })
                .setCancelable(false)
                .show();
    }

    // Вызываем BiometricPrompt, а по успеху — в MainActivity
    private void promptBiometricAndEnter() {
        new BiometricHelper(this)
                .authenticate(this::startMain);
    }

    // Запуск основного экрана
    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // --- callbacks из фрагментов ---

    @Override
    public void onLoginSuccess() {
        // после логина предлагаем включить биометрию
        askUseBiometric();
    }

    @Override
    public void onRegisterSuccess() {
        // после регистрации сразу в LoginFragment
        showLogin();
    }
}
