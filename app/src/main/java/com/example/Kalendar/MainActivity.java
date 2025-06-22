package com.example.Kalendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.fragments.CalendarFragment;
import com.example.Kalendar.fragments.HomeFragment;
import com.example.Kalendar.fragments.ProfileFragment;
import com.example.Kalendar.viewmodel.MainViewModel;
import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    private MainViewModel viewModel;
    private int userId;

    // Навигационные элементы
    private LinearLayout navHomeContainer, navCalendarContainer, navProfileContainer;
    private TextView navHome, navCalendar, navProfile;
    private ImageView navHomeIcon, navCalendarIcon, navProfileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Отключаем ночной режим, ставим глобальный обработчик крешей и инициализируем ThreeTenABP
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Log.e("FATAL", "UNCAUGHT ERROR", throwable)
        );
        AndroidThreeTen.init(this);

        // Проверяем сессию
        userId = SessionManager.getLoggedInUserId(this);
        if (userId == -1) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // Основной layout сразу отрисовываем
        setContentView(R.layout.activity_main);

        // Подготовка ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getInitComplete().observe(this, isReady -> {
            if (Boolean.TRUE.equals(isReady)) {
                // Как только бэкенд-инициализация завершена, включаем навигацию и разрешения
                setupNavigation();
                requestNotificationPermissionIfNeeded();
            }
        });

        // Запускаем инициализацию календаря/дня в фоновом потоке
        viewModel.initForUser(userId);
    }

    private void setupNavigation() {
        navHomeContainer     = findViewById(R.id.nav_home_container);
        navCalendarContainer = findViewById(R.id.nav_calendar_container);
        navProfileContainer  = findViewById(R.id.nav_profile_container);

        navHome     = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navProfile  = findViewById(R.id.nav_profile);

        navHomeIcon     = findViewById(R.id.nav_home_icon);
        navCalendarIcon = findViewById(R.id.nav_calendar_icon);
        navProfileIcon  = findViewById(R.id.nav_profile_icon);

        // Задаём стартовый таб
        selectTab("home");
        loadFragment(new HomeFragment());

        navHomeContainer.setOnClickListener(v -> {
            selectTab("home");
            loadFragment(new HomeFragment());
        });
        navCalendarContainer.setOnClickListener(v -> {
            selectTab("calendar");
            loadFragment(new CalendarFragment());
        });
        navProfileContainer.setOnClickListener(v -> {
            selectTab("profile");
            loadFragment(new ProfileFragment());
        });
    }
    private void selectTab(String tab) {
        int inactive = Color.parseColor("#888888");
        navHome.setTextColor(inactive);
        navCalendar.setTextColor(inactive);
        navProfile.setTextColor(inactive);
        navHomeIcon.setColorFilter(inactive);
        navCalendarIcon.setColorFilter(inactive);
        navProfileIcon.setImageResource(R.drawable.ic_profile);

        int active = Color.BLACK;
        switch (tab) {
            case "home":
                navHome.setTextColor(active);
                navHomeIcon.setColorFilter(active);
                break;
            case "calendar":
                navCalendar.setTextColor(active);
                navCalendarIcon.setColorFilter(active);
                break;
            case "profile":
                navProfile.setTextColor(active);
                navProfileIcon.setImageResource(R.drawable.ic_profile_active);
                break;
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Разрешить уведомления?")
                            .setMessage("Чтобы приложение могло напоминать Вам о задачах и событиях, нужно разрешение на отправку уведомлений.")
                            .setPositiveButton("Разрешить", (d, w) ->
                                    ActivityCompat.requestPermissions(this,
                                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                            REQUEST_CODE_POST_NOTIFICATIONS))
                            .setNegativeButton("Не сейчас", null)
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_CODE_POST_NOTIFICATIONS);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Уведомления запрещены", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
