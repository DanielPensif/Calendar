package com.example.Kalendar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.Kalendar.adapters.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsNotificationsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;
    private SwitchMaterial notificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notifications);

        // Проверка авторизации
        if (SessionManager.getLoggedInUserId(this) == -1) {
            finish();
            return;
        }

        notificationSwitch = findViewById(R.id.notificationSwitch);

        // Установка начального состояния switch (можно загрузить из SharedPreferences)
        boolean notificationsEnabled = checkNotificationPermission();
        notificationSwitch.setChecked(notificationsEnabled);

        // Обработчик изменения состояния switch
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    requestNotificationPermissionIfNeeded();
                    Toast.makeText(SettingsNotificationsActivity.this,
                            "Уведомления вкючены", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsNotificationsActivity.this,
                            "Уведомления отключены", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Для версий ниже Android 13 разрешение не требуется
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Разрешить уведомления?")
                            .setMessage("Чтобы получать напоминания о задачах, разрешите уведомления")
                            .setPositiveButton("Разрешить", (d, w) -> requestNotificationPermission())
                            .setNegativeButton("Отмена", (d, w) -> notificationSwitch.setChecked(false))
                            .show();
                } else {
                    requestNotificationPermission();
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_POST_NOTIFICATIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
                notificationSwitch.setChecked(true);
            } else {
                Toast.makeText(this, "Уведомления запрещены", Toast.LENGTH_SHORT).show();
                notificationSwitch.setChecked(false);
            }
        }
    }
}