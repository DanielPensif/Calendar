package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.R;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView fullNameTextView;
    private TextView usernameTextView;
    private TextView bioTextView;
    private Button statsButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        profileImageView = view.findViewById(R.id.profileImageView);
        fullNameTextView = view.findViewById(R.id.fullNameTextView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        bioTextView = view.findViewById(R.id.bioTextView);
        statsButton = view.findViewById(R.id.statsButton);

        fullNameTextView.setText("Имя пользователя");
        usernameTextView.setText("@username");
        bioTextView.setText("Описание профиля");

        statsButton.setOnClickListener(v -> {

        });
    }
}
