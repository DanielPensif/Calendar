package com.example.Kalendar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.UserEntity;
import com.example.Kalendar.utils.PasswordUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment {

    public interface OnRegisterSuccessListener {
        void onRegisterSuccess();
    }

    private OnRegisterSuccessListener listener;
    private EditText etUsername, etPassword;
    private Button btnRegister;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterSuccessListener) {
            listener = (OnRegisterSuccessListener) context;
        } else {
            throw new IllegalStateException("Activity must implement OnRegisterSuccessListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        etUsername   = v.findViewById(R.id.etUsername);
        etPassword   = v.findViewById(R.id.etPassword);
        btnRegister  = v.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(view -> doRegister());

        return v;
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(),
                    "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            // Проверяем, что логин уникален
            if (db.userDao().getByUsername(username) != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Пользователь с таким именем уже существует",
                                Toast.LENGTH_SHORT).show()
                );
            } else {
                // Создаём нового юзера
                UserEntity user = new UserEntity();
                user.username     = username;
                user.passwordHash = PasswordUtils.hash(password);
                db.userDao().insert(user);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    listener.onRegisterSuccess();
                });
            }
        });
    }
}