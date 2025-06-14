package com.example.Kalendar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.Kalendar.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.Kalendar.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {
    public interface OnRegisterSuccessListener { void onRegisterSuccess(); }
    private OnRegisterSuccessListener listener;
    private AuthViewModel viewModel;
    private TextInputLayout usernameLayout,passwordLayout,repeatLayout;

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterSuccessListener) listener = (OnRegisterSuccessListener) context;
        else throw new IllegalStateException("Activity must implement OnRegisterSuccessListener");
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_register,c,false);
        usernameLayout = v.findViewById(R.id.usernameLayout);
        passwordLayout = v.findViewById(R.id.passwordInputLayout);
        repeatLayout   = v.findViewById(R.id.repeatPasswordInputLayout);
        v.findViewById(R.id.btnRegister).setOnClickListener(xx -> doRegister());
        v.findViewById(R.id.tvGoLogin).setOnClickListener(xx ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_right)
                        .replace(R.id.auth_container,new LoginFragment()).commit()
        );
        passwordLayout.setEndIconOnClickListener(vv -> toggle(passwordLayout));
        repeatLayout.setEndIconOnClickListener(vv -> toggle(repeatLayout));
        return v;
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v,b);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        viewModel.registerResult.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) listener.onRegisterSuccess();
        });
        viewModel.error.observe(getViewLifecycleOwner(), msg -> Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show());
    }

    private void doRegister() {
        String u = usernameLayout.getEditText().getText().toString().trim();
        String p = passwordLayout.getEditText().getText().toString();
        String r = repeatLayout.getEditText().getText().toString();
        viewModel.register(u,p,r);
    }

    private void toggle(TextInputLayout layout) {
        TextInputEditText et = (TextInputEditText) layout.getEditText();
        if (et.getInputType()==(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        layout.setEndIconDrawable(et.getInputType()==(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD)
                ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
        et.setSelection(et.getText().length());
    }
}

