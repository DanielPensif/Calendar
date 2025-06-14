package com.example.Kalendar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.Kalendar.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.Kalendar.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {
    public interface OnAuthSuccessListener { void onLoginSuccess(); }
    private OnAuthSuccessListener listener;
    private AuthViewModel viewModel;
    private TextInputLayout usernameLayout, passwordLayout;

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAuthSuccessListener) listener = (OnAuthSuccessListener) context;
        else throw new IllegalStateException("Activity must implement OnAuthSuccessListener");
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_login, c, false);
        usernameLayout = v.findViewById(R.id.usernameLayout);
        passwordLayout = v.findViewById(R.id.passwordInputLayout);
        v.findViewById(R.id.btnLogin).setOnClickListener(xx -> doLogin());
        v.findViewById(R.id.tvGoRegister).setOnClickListener(xx ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                        .replace(R.id.auth_container, new RegisterFragment()).commit()
        );
        passwordLayout.setEndIconOnClickListener(vv -> toggleVisibility());
        return v;
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v,b);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        viewModel.loginResult.observe(getViewLifecycleOwner(), user -> { if (user != null) listener.onLoginSuccess(); });
        viewModel.error.observe(getViewLifecycleOwner(), msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
    }

    private void doLogin() {
        String u = usernameLayout.getEditText().getText().toString().trim();
        String p = passwordLayout.getEditText().getText().toString();
        viewModel.login(u,p);
    }

    private void toggleVisibility() {
        TextInputEditText et = (TextInputEditText) passwordLayout.getEditText();
        if (et.getInputType()==(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility_off);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_visibility);
        }
        et.setSelection(et.getText().length());
    }
}