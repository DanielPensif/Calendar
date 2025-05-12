package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterFragment extends Fragment {

    public interface OnRegisterSuccessListener {
        void onRegisterSuccess();
    }

    private OnRegisterSuccessListener listener;
    private EditText etUsername, etPassword, etRepeatPassword;
    private Button btnRegister;
    private TextInputLayout passwordInputLayout, repeatPasswordInputLayout;
    private TextView tvGoLogin;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterSuccessListener) {
            listener = (OnRegisterSuccessListener) context;
        } else {
            throw new IllegalStateException("Activity must implement OnRegisterSuccessListener");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        etUsername   = v.findViewById(R.id.etUsername);
        etPassword   = v.findViewById(R.id.etPassword);
        btnRegister  = v.findViewById(R.id.btnRegister);
        passwordInputLayout = v.findViewById(R.id.passwordInputLayout);
        repeatPasswordInputLayout = v.findViewById(R.id.repeatPasswordInputLayout);
        etRepeatPassword = v.findViewById(R.id.etRepeatPassword);
        tvGoLogin = v.findViewById(R.id.tvGoLogin);
        btnRegister.setOnClickListener(view -> doRegister());
        passwordInputLayout.setEndIconOnClickListener(setVisibility1());
        repeatPasswordInputLayout.setEndIconOnClickListener(setVisibility2());
        tvGoLogin.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left,  // новый фрагмент входит слева
                            R.anim.slide_out_right // текущий уходит вправо
                    )
                    .replace(R.id.auth_container, new LoginFragment())
                    .commit();
        });
        return v;
    }

    private void doRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
            Toast.makeText(requireContext(),
                    "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repeatPassword)) {
            Toast.makeText(requireContext(),
                    "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            if (db.userDao().getByUsername(username) != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Пользователь с таким именем уже существует",
                                Toast.LENGTH_SHORT).show()
                );
            } else {
                UserEntity user = new UserEntity();
                user.username = username;
                user.passwordHash = PasswordUtils.hash(password);
                db.userDao().insert(user);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show();
                    listener.onRegisterSuccess();
                });
            }
        });
    }
    private View.OnClickListener setVisibility1() {
        return view -> {
            TextInputEditText editText = (TextInputEditText) passwordInputLayout.getEditText();
            if (editText != null) {
                int selection = editText.getSelectionEnd();
                if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Показать пароль
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility);
                } else {
                    // Скрыть пароль
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordInputLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
                }
                editText.setSelection(selection);
            }
        };
    }
    private View.OnClickListener setVisibility2() {
        return view -> {
            TextInputEditText editText = (TextInputEditText) repeatPasswordInputLayout.getEditText();
            if (editText != null) {
                int selection = editText.getSelectionEnd();
                if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Показать пароль
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    repeatPasswordInputLayout.setEndIconDrawable(R.drawable.ic_visibility);
                } else {
                    // Скрыть пароль
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    repeatPasswordInputLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
                }
                editText.setSelection(selection);
            }
        };
    }
}