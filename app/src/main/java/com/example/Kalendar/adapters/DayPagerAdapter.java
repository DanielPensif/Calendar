package com.example.Kalendar.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.Kalendar.fragments.EventsFragment;
import com.example.Kalendar.fragments.TasksFragment;
import org.threeten.bp.LocalDate;

public class DayPagerAdapter extends FragmentStateAdapter {
    private final LocalDate date;
    public DayPagerAdapter(@NonNull FragmentActivity fa, LocalDate date) {
        super(fa);
        this.date = date;
    }
    @NonNull @Override
    public Fragment createFragment(int pos) {
        return pos == 0
                ? TasksFragment.newInstance(date)
                : EventsFragment.newInstance(date);
    }
    @Override public int getItemCount() { return 2; }
}