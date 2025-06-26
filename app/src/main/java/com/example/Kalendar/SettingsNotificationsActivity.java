package com.example.Kalendar;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsNotificationsActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "kalendar_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private SwitchMaterial notificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notifications);

        createNotificationChannel();
        initViews();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Календарь уведомлений",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void initViews() {
        notificationSwitch = findViewById(R.id.notificationSwitch);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                cancelNotification();
            }
        });
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_ID
                );
            } else {
                showTestNotification();
            }
        } else {
            showTestNotification();
        }
    }

    private void showTestNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Тестовое уведомление")
                .setContentText("Уведомления включены")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        try {
            manager.notify(NOTIFICATION_ID, builder.build());
            Toast.makeText(this, "Тестовое уведомление отправлено", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Ошибка: нет разрешения на уведомления", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, "Уведомления выключены", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showTestNotification();
            } else {
                notificationSwitch.setChecked(false);
                Toast.makeText(this, "Разрешение не получено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}