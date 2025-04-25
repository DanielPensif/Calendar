package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.Kalendar.adapters.DayPagerAdapter;
import com.example.Kalendar.fragments.AddEventDialogFragment;
import com.example.Kalendar.fragments.AddTaskDialogFragment;
import com.example.Kalendar.fragments.EventsFragment;
import com.example.Kalendar.fragments.TasksFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Objects;
import java.util.Locale;

public class DayDetailsActivity extends AppCompatActivity {

    private LocalDate selectedDate;
    private TextView dateHeader;
    private TabLayout tabLayout;
    private DayPagerAdapter adapter;

    private AddEventDialogFragment eventDialog;
    private AddTaskDialogFragment taskDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_details);

        // Добавляем Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Получаем дату из интента
        String dateStr = getIntent().getStringExtra("date");
        selectedDate = LocalDate.parse(Objects.requireNonNull(dateStr));

        // Инициализируем заголовок с датой
        dateHeader = findViewById(R.id.dateHeader);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));
        dateHeader.setText(selectedDate.format(formatter));

        // Диалоги добавления
        eventDialog = AddEventDialogFragment.newInstance(selectedDate);
        eventDialog.setOnEventSavedListener(() -> {
            int tab = tabLayout.getSelectedTabPosition();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + tab);
            if (fragment instanceof TasksFragment && tab == 0) {
                ((TasksFragment) fragment).refresh();
            } else if (fragment instanceof EventsFragment && tab == 1) {
                ((EventsFragment) fragment).refresh();
            }
        });

        // ViewPager и вкладки
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        FloatingActionButton fab = findViewById(R.id.fab);

        adapter = new DayPagerAdapter(this, selectedDate);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Задачи" : "События")
        ).attach();

        tabLayout.setBackgroundColor(Color.WHITE);

        // Обработка нажатия FAB
        fab.setOnClickListener(v -> {
            int tab = tabLayout.getSelectedTabPosition();
            if (tab == 0) {
                taskDialog = AddTaskDialogFragment.newInstance(selectedDate);
                taskDialog.setOnTaskSavedListener(() -> {
                    Fragment f = getSupportFragmentManager().findFragmentByTag("f0");
                    if (f instanceof TasksFragment) {
                        ((TasksFragment) f).refresh();
                    }
                });
                taskDialog.show(getSupportFragmentManager(), "addTask");
            } else {
                int calendarId = getIntent().getIntExtra("calendarId", -1);
                eventDialog.setPreselectedCalendarId(calendarId);
                eventDialog.show(getSupportFragmentManager(), "addEvent");
            }
        });
    }
}
