package com.example.Kalendar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.Kalendar.R;
import com.example.Kalendar.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    public interface OnAuthSuccessListener {
        void onLoginSuccess();
    }

    private OnAuthSuccessListener listener;
    private AuthViewModel viewModel;

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthSuccessListener) {
            listener = (OnAuthSuccessListener) context;
        } else {
            throw new IllegalStateException(
                    "Activity must implement LoginFragment.OnAuthSuccessListener"
            );
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        // UI-элементы
        usernameLayout = v.findViewById(R.id.usernameLayout);
        passwordLayout = v.findViewById(R.id.passwordInputLayout);
        btnLogin       = v.findViewById(R.id.btnLogin);
        progressBar    = v.findViewById(R.id.progressBar);

        // Скрываем ProgressBar по умолчанию
        progressBar.setVisibility(View.GONE);

        btnLogin.setOnClickListener(x -> doLogin());

        v.findViewById(R.id.tvGoRegister).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.auth_container, new RegisterFragment())
                        .commit()
        );

        // иконка «показать/скрыть пароль»
        passwordLayout.setEndIconOnClickListener(xx -> toggleVisibility());

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ОБРАТИТЕ ВНИМАНИЕ: создаём ViewModel у Activity, чтобы это был тот же экземпляр
        viewModel = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        // Наблюдаем за результатом login
        viewModel.loginResult.observe(getViewLifecycleOwner(), user -> {
            // Всегда сначала вернуть UI в норму
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            if (user != null) {
                // Успешный вход
                listener.onLoginSuccess();
            }
        });

        // Наблюдаем за ошибками
        viewModel.error.observe(getViewLifecycleOwner(), msg -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void doLogin() {
        String u = usernameLayout.getEditText().getText().toString().trim();
        String p = passwordLayout.getEditText().getText().toString();

        // Валидация
        boolean ok = true;
        if (u.isEmpty()) {
            usernameLayout.setError("Введите имя пользователя");
            ok = false;
        } else {
            usernameLayout.setError(null);
        }
        if (p.isEmpty()) {
            passwordLayout.setError("Введите пароль");
            ok = false;
        } else {
            passwordLayout.setError(null);
        }
        if (!ok) return;

        // Показываем индикатор и блокируем кнопку
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Запускаем фоновую проверку
        viewModel.login(u, p);
    }


    private void toggleVisibility() {
        TextInputEditText et = (TextInputEditText) passwordLayout.getEditText();
        if (et.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility);
        }
        et.setSelection(et.getText().length());
    }
}
