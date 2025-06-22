package com.example.Kalendar;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DayDetailsActivity extends AppCompatActivity {

    private LocalDate selectedDate;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_details);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // дата
        selectedDate = LocalDate.parse(Objects.requireNonNull(getIntent().getStringExtra("date")));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));
        ((android.widget.TextView)findViewById(R.id.dateHeader))
                .setText(selectedDate.format(fmt));

        // ViewPager + tabs
        ViewPager2 pager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        DayPagerAdapter adapter = new DayPagerAdapter(this, selectedDate);
        pager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, pager, (tab, pos) ->
                tab.setText(pos == 0 ? "Задачи" : "События")
        ).attach();
        tabLayout.setBackgroundColor(Color.WHITE);

        // FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            int tab = tabLayout.getSelectedTabPosition();
            if (tab == 0) {
                AddTaskDialogFragment dlg = AddTaskDialogFragment.newInstance(selectedDate);
                dlg.setOnTaskSavedListener(() -> refreshCurrent());
                dlg.show(getSupportFragmentManager(), "addTask");
            } else {
                int preCal = getIntent().getIntExtra("calendarId", -1);
                AddEventDialogFragment dlg = AddEventDialogFragment.newInstance(selectedDate);
                dlg.setPreselectedCalendarId(preCal);
                dlg.setOnEventSavedListener(() -> refreshCurrent());
                dlg.show(getSupportFragmentManager(), "addEvent");
            }
        });
    }

    private void refreshCurrent() {
        // find current fragment by tag "f0"/"f1" and call refresh()
        Fragment f = getSupportFragmentManager().findFragmentByTag("f" + tabLayout.getSelectedTabPosition());
        if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
        else if (f instanceof EventsFragment) ((EventsFragment) f).refresh();
    }
}