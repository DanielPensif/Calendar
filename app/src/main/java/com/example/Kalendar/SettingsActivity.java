package com.example.Kalendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.Kalendar.fragments.ProfileFragment;

public class SettingsActivity extends AppCompatActivity {

    private TextView buttonNotifications;
    private TextView buttonStyle;
    private TextView buttonProfile;
    private TextView buttonSecurity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        buttonNotifications = findViewById(R.id.notificationsButton);
        buttonStyle = findViewById(R.id.styleButton);
        buttonProfile = findViewById(R.id.profileButton);
        buttonSecurity = findViewById(R.id.securityButton);

        buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SettingsNotificationsActivity.class);
                startActivity(intent);
            }
        });

        buttonSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SettingsSecurityActivity.class);
                startActivity(intent);
            }
        });

        buttonStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SettingsStyleActivity.class);
                startActivity(intent);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SettingsProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}