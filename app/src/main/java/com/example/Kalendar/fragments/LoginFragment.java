package com.example.Kalendar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.utils.PasswordUtils;
import com.example.Kalendar.adapters.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    public interface OnAuthSuccessListener {
        void onLoginSuccess();
    }

    private OnAuthSuccessListener listener;
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthSuccessListener) {
            listener = (OnAuthSuccessListener) context;
        } else {
            throw new IllegalStateException("Activity must implement OnAuthSuccessListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername   = v.findViewById(R.id.etUsername);
        etPassword   = v.findViewById(R.id.etPassword);
        btnLogin     = v.findViewById(R.id.btnLogin);
        tvGoRegister = v.findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(view -> doLogin());
        tvGoRegister.setOnClickListener(view -> {
            // Переходим на фрагмент регистрации
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_container, new RegisterFragment())
                    .commit();
        });

        return v;
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(),
                    "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Работа с БД в фоновом потоке
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            UserEntity user = db.userDao().getByUsername(username);
            if (user != null && user.passwordHash.equals(PasswordUtils.hash(password))) {
                // Сохраняем сессию
                SessionManager.saveUser(requireContext(), user.id);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Успешный вход", Toast.LENGTH_SHORT).show();
                    listener.onLoginSuccess();
                });
            } else {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}