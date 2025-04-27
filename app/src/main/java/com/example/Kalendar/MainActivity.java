package com.example.Kalendar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                Log.e("FATAL", "UNCAUGHT ERROR", throwable)
        );
        com.jakewharton.threetenabp.AndroidThreeTen.init(this);

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
    }

    private void selectTab(String tab) {
        int inactive = Color.parseColor("#888888");
        navHome.setTextColor(inactive);
        navCalendar.setTextColor(inactive);
        navProfile.setTextColor(inactive);
        navHomeIcon.setColorFilter(inactive);
        navCalendarIcon.setColorFilter(inactive);
        navProfileIcon.setColorFilter(inactive);

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
                navProfileIcon.setColorFilter(active);
                break;
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
            List<CalendarEntity> all = db.calendarDao().getAll();
            int calendarId;
            if (all.isEmpty()) {
                CalendarEntity cal = new CalendarEntity(
                        "Календарь по умолчанию",
                        System.currentTimeMillis(),
                        "#67BA80"
                );
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
