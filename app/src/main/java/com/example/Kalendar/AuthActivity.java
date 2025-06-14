package com.example.Kalendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.fragments.LoginFragment;
import com.example.Kalendar.fragments.RegisterFragment;
import com.example.Kalendar.viewmodel.AuthViewModel;
import com.example.Kalendar.utils.BiometricHelper;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity
        implements LoginFragment.OnAuthSuccessListener,
        RegisterFragment.OnRegisterSuccessListener {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        observeViewModel();

        int userId = SessionManager.getLoggedInUserId(this);
        if (userId != -1) {
            if (!SessionManager.isBiometricAsked(this)) askUseBiometric();
            else if (SessionManager.isBiometricEnabled(this)) promptBiometricAndEnter();
            else startMain();
        } else showLogin();
    }

    private void observeViewModel() {
        viewModel.loginResult.observe(this, user -> {
            if (user != null) {
                SessionManager.saveUser(this, user.id);
                onLoginSuccess();
            }
        });
        viewModel.registerResult.observe(this, success -> {
            if (Boolean.TRUE.equals(success)) Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
        });
        viewModel.error.observe(this, msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void showLogin() { replaceFragment(new LoginFragment()); }

    private void replaceFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, f)
                .commit();
    }

    private void askUseBiometric() {
        new AlertDialog.Builder(this)
                .setTitle("Включить вход по биометрии?")
                .setMessage("Хотите использовать отпечаток или код устройства для скрытия данных?")
                .setPositiveButton("Да", (d,w) -> {
                    SessionManager.setBiometricEnabled(this, true);
                    SessionManager.setBiometricAsked(this, true);
                    promptBiometricAndEnter();
                })
                .setNegativeButton("Нет", (d,w) -> {
                    SessionManager.setBiometricEnabled(this, false);
                    SessionManager.setBiometricAsked(this, true);
                    startMain();
                }).setCancelable(false).show();
    }

    private void promptBiometricAndEnter() {
        new BiometricHelper(this).authenticate(this::startMain);
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class)); finish();
    }

    @Override public void onLoginSuccess() { askUseBiometric(); }
    @Override public void onRegisterSuccess() { showLogin(); }
}