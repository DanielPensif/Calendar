package com.example.Kalendar;

import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class CalendarFragment extends Fragment {

    private LinearLayout calendarsContainer;
    private DrawerLayout drawerLayout;
    private ImageView menuButton, addCalendarIcon;
    private LinearLayout leftDrawer;
    private FrameLayout menuButtonContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarsContainer = view.findViewById(R.id.calendarsContainer);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        menuButton = view.findViewById(R.id.menuButton);
        addCalendarIcon = view.findViewById(R.id.addCalendarBtn);
        leftDrawer = view.findViewById(R.id.leftDrawer);

        addCalendarIcon.setOnClickListener(v -> addNewCalendar());
        menuButton.setOnClickListener(v -> toggleMenuDrawer());
        menuButtonContainer = view.findViewById(R.id.menuButtonContainer);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // смещаем кнопку в зависимости от слайда панели
                float moveX = drawerView.getWidth() * slideOffset;
                menuButtonContainer.setTranslationX(moveX);
            }

            @Override public void onDrawerOpened(@NonNull View drawerView) {}
            @Override public void onDrawerClosed(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
        });
        setupWeekDays(view);

        return view;
    }

    private void setupWeekDays(View root) {
        int[] dayIds = {
                R.id.dayMon, R.id.dayTue, R.id.dayWed,
                R.id.dayThu, R.id.dayFri, R.id.daySat, R.id.daySun
        };

        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

        for (int i = 0; i < dayIds.length; i++) {
            View dayCircle = root.findViewById(dayIds[i]);
            int finalI = i;

            dayCircle.setOnClickListener(v -> openDayInfo(days[finalI]));
        }
    }

    private void toggleMenuDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void openDayInfo(String day) {
        Intent intent = new Intent(getContext(), DayInfoFull.class);
        intent.putExtra("date", day);
        startActivity(intent);
    }

    private void addNewCalendar() {
        Button newCalendar = new Button(getContext());
        newCalendar.setText("Новый календарь");
        newCalendar.setAllCaps(false);
        calendarsContainer.addView(newCalendar);
    }
}
