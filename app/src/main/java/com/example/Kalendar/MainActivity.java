package com.example.Kalendar;

import android.graphics.*;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHomeContainer, navCalendarContainer;
    private ImageView navHomeIcon, navCalendarIcon;
    private TextView navHome, navCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navHomeContainer = findViewById(R.id.nav_home_container);
        navCalendarContainer = findViewById(R.id.nav_calendar_container);
        navHome = findViewById(R.id.nav_home);
        navCalendar = findViewById(R.id.nav_calendar);
        navHomeIcon = findViewById(R.id.nav_home_icon);
        navCalendarIcon = findViewById(R.id.nav_calendar_icon);

        // начальный экран
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
        // сброс цветов
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
}
