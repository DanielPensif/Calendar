package com.example.Kalendar;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.Objects;

public class DayDetailsActivity extends AppCompatActivity {

    private LocalDate selectedDate;
    private TabLayout tabLayout;
    private DayPagerAdapter adapter;

    private AddEventDialogFragment eventDialog;
    private AddTaskDialogFragment taskDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_details);

        String dateStr = getIntent().getStringExtra("date");
        selectedDate = LocalDate.parse(Objects.requireNonNull(dateStr));;

        eventDialog = AddEventDialogFragment.newInstance(selectedDate);
        eventDialog.setOnEventSavedListener(() -> {
            int tab = tabLayout.getSelectedTabPosition();
            if (adapter == null) {
                return;
            }
            if (tab == 0) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentByTag("f" + tab);
                if (fragment instanceof TasksFragment) {
                    ((TasksFragment) fragment).refresh();
                }
            }
            else if (tab == 1) {
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentByTag("f" + tab);
                if (fragment instanceof EventsFragment) {
                    ((EventsFragment) fragment).refresh();
                }
            }
        });


        ViewPager2 viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        FloatingActionButton fab = findViewById(R.id.fab);

        adapter = new DayPagerAdapter(this, selectedDate);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Задачи" : "События")
        ).attach();
        tabLayout.setBackgroundColor(Color.WHITE);
        fab.setOnClickListener(v -> {
            int tab = tabLayout.getSelectedTabPosition();
            if (tab == 0) {
                taskDialog = AddTaskDialogFragment.newInstance(selectedDate);
                taskDialog.setOnTaskSavedListener(() -> {
                    Fragment f = getSupportFragmentManager().findFragmentByTag("f0");
                    if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
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
