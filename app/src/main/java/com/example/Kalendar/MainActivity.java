package com.example.Kalendar;;

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

import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.CalendarFragment;
import com.example.Kalendar.fragments.HomeFragment;
import com.example.Kalendar.fragments.ProfileFragment;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView navHomeIcon, navCalendarIcon, navProfileIcon;
    private TextView navHome, navCalendar, navProfile;
    private AppDatabase db;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Log.e("FATAL", "UNCAUGHT ERROR", throwable)
        );
        com.jakewharton.threetenabp.AndroidThreeTen.init(this);
        userId = SessionManager.getLoggedInUserId(this);
        if (SessionManager.getLoggedInUserId(this) == -1) {
            // не залогинен — переходим на AuthActivity
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        // Кнопки навигации
        LinearLayout navHomeContainer     = findViewById(R.id.nav_home_container);
        LinearLayout navCalendarContainer = findViewById(R.id.nav_calendar_container);
        LinearLayout navProfileContainer  = findViewById(R.id.nav_profile_container);

        navHome     = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navProfile  = findViewById(R.id.nav_profile);

        navHomeIcon     = findViewById(R.id.nav_home_icon);
        navCalendarIcon = findViewById(R.id.nav_calendar_icon);
        navProfileIcon  = findViewById(R.id.nav_profile_icon);

        // Room
        db = AppDatabase.getDatabase(this);
        initTodayIfNotExists();

        // Стартовый фрагмент
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
        requestNotificationPermissionIfNeeded();
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
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Опционально: показать rationale, если отказали ранее
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Разрешить уведомления?")
                            .setMessage("Чтобы приложение могло напоминать вам о задачах и событиях, нужно разрешение на отправку уведомлений.")
                            .setPositiveButton("Разрешить", (d, w) -> {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        REQUEST_CODE_POST_NOTIFICATIONS);
                            })
                            .setNegativeButton("Не сейчас", null)
                            .show();
                } else {
                    // Первый запрос или после «Не спрашивать»
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
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void initTodayIfNotExists() {
        new Thread(() -> {
            List<CalendarEntity> all = db.calendarDao().getByUserId(userId);
            int calendarId;
            if (all.isEmpty()) {
                CalendarEntity cal = new CalendarEntity(
                        "Календарь по умолчанию",
                        System.currentTimeMillis(),
                        "#67BA80",
                        userId
                );
                cal.userId = userId;
                calendarId = (int) db.calendarDao().insert(cal);
            } else {
                calendarId = all.get(0).id;
            }

            long ts = getTodayMidnightTimestamp();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(ts, calendarId);
            if (day == null) {
                day = new DayEntity();
                day.timestamp = ts;
                day.calendarId = calendarId;
                long id = db.dayDao().insert(day);
                Log.d("DB", "Создан день id=" + id);
            } else {
                Log.d("DB", "День уже есть id=" + day.id);
            }
        }).start();
    }

    private long getTodayMidnightTimestamp() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,   0);
        c.set(Calendar.MINUTE,        0);
        c.set(Calendar.SECOND,        0);
        c.set(Calendar.MILLISECOND,   0);
        return c.getTimeInMillis();
    }
}
