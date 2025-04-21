package com.example.Kalendar;

import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.CalendarFragment;
import com.example.Kalendar.fragments.HomeFragment;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView navHomeIcon, navCalendarIcon;
    private TextView navHome, navCalendar;

    private AppDatabase db;  // Room Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("FATAL", "UNCAUGHT ERROR", throwable);
        });

        com.jakewharton.threetenabp.AndroidThreeTen.init(this);

        setContentView(R.layout.activity_main);

        LinearLayout navHomeContainer = findViewById(R.id.nav_home_container);
        LinearLayout navCalendarContainer = findViewById(R.id.nav_calendar_container);
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navHomeIcon = findViewById(R.id.nav_home_icon);
        navCalendarIcon = findViewById(R.id.nav_calendar_icon);

        // Получаем ссылку на Room DB
        db = AppDatabase.getDatabase(this);

        // Пример: создаём день на сегодня (потом перенесём в CalendarFragment)
        initTodayIfNotExists();

        // Загрузка стартового фрагмента
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
    }

    private void selectTab(String tab) {
        navHome.setTextColor(Color.parseColor("#888888"));
        navCalendar.setTextColor(Color.parseColor("#888888"));
        navHomeIcon.setColorFilter(Color.parseColor("#888888"));
        navCalendarIcon.setColorFilter(Color.parseColor("#888888"));

        if (tab.equals("home")) {
            navHome.setTextColor(Color.BLACK);
            navHomeIcon.setColorFilter(Color.BLACK);
        } else if (tab.equals("calendar")) {
            navCalendar.setTextColor(Color.BLACK);
            navCalendarIcon.setColorFilter(Color.BLACK);
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
            // 1. Получаем или создаём календарь по умолчанию
            List<CalendarEntity> all = db.calendarDao().getAll();
            int calendarId;

            if (all.isEmpty()) {
                CalendarEntity calendar = new CalendarEntity("Календарь по умолчанию", System.currentTimeMillis(), "#67BA80");
                calendarId = (int) db.calendarDao().insert(calendar);
            } else {
                calendarId = all.get(0).id;
            }

            // 2. Инициализируем день
            long timestamp = getTodayMidnightTimestamp();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(timestamp, calendarId);

            if (day == null) {
                day = new DayEntity();
                day.timestamp = timestamp;
                day.calendarId = calendarId;
                long id = db.dayDao().insert(day);
                Log.d("DB", "Создан день с id = " + id);
            } else {
                Log.d("DB", "День уже существует: id = " + day.id);
            }
        }).start();
    }


    private long getTodayMidnightTimestamp() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
