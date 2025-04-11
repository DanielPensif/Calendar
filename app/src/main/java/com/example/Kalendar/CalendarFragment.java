package com.example.Kalendar;

import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarFragment extends Fragment {

    private LinearLayout calendarsContainer;
    private DrawerLayout drawerLayout;
    private TextView dayInfoDate;
    private ImageView menuButton, addCalendarIcon;
    private LinearLayout leftDrawer, rightDrawer;
    private RecyclerView dayInfoTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarsContainer = view.findViewById(R.id.calendarsContainer);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        dayInfoDate = view.findViewById(R.id.dayInfoDate);
        dayInfoTasks = view.findViewById(R.id.dayInfoTasks);
        menuButton = view.findViewById(R.id.menuButton);
        addCalendarIcon = view.findViewById(R.id.addCalendarBtn);
        leftDrawer = view.findViewById(R.id.leftDrawer);
        rightDrawer = view.findViewById(R.id.rightDrawer);

        addCalendarIcon.setOnClickListener(v -> addNewCalendar());
        menuButton.setOnClickListener(v -> toggleMenuDrawer());

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

            dayCircle.setOnClickListener(v -> toggleDayInfoPanel(days[finalI]));
        }
    }

    private void toggleMenuDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void toggleDayInfoPanel(String day) {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            dayInfoDate.setText(day);
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private void addNewCalendar() {
        Button newCalendar = new Button(getContext());
        newCalendar.setText("Новый календарь");
        newCalendar.setAllCaps(false);
        calendarsContainer.addView(newCalendar);
    }
}
