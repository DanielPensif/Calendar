package com.example.Kalendar;


import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.*;
import androidx.drawerlayout.widget.*;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarActivity extends AppCompatActivity {

    private LinearLayout calendarsContainer;
    private DrawerLayout menuDrawerLayout;
    private DrawerLayout dayInfoDrawerLayout;
    private TextView dayInfoDate;
    ImageView menuButton;
    ImageView addCalendarIcon;
    LinearLayout leftDrawer;
    LinearLayout rightDrawer;
    private RecyclerView dayInfoTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarsContainer = findViewById(R.id.calendarsContainer);
        menuDrawerLayout = findViewById(R.id.drawerLayout);
        dayInfoDate = findViewById(R.id.dayInfoDate);
        dayInfoTasks = findViewById(R.id.dayInfoTasks);
        dayInfoDrawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.menuButton);
        addCalendarIcon = findViewById(R.id.addCalendarBtn);
        leftDrawer = findViewById(R.id.leftDrawer);
        rightDrawer = findViewById(R.id.rightDrawer);


        addCalendarIcon.setOnClickListener(v -> addNewCalendar());
        menuButton.setOnClickListener(v -> toggleMenuDrawer());

        setupWeekDays();
    }
    private void setupWeekDays() {
        int[] dayIds = {
                R.id.dayMon, R.id.dayTue, R.id.dayWed,
                R.id.dayThu, R.id.dayFri, R.id.daySat, R.id.daySun
        };

        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

        for (int i = 0; i < dayIds.length; i++) {
            View dayCircle = findViewById(dayIds[i]);
            int finalI = i;

            dayCircle.setOnClickListener(v -> toggleDayInfoPanel(days[finalI]));
        }
    }
    private void toggleMenuDrawer() {
        if (menuDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            menuDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            menuDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void toggleDayInfoPanel(String day) {
        if (dayInfoDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            dayInfoDate.setText(day);
            dayInfoDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            dayInfoDrawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void addNewCalendar() {
        Button newCalendar = new Button(this);
        newCalendar.setText("Новый календарь");
        newCalendar.setAllCaps(false);
        calendarsContainer.addView(newCalendar);
    }
}