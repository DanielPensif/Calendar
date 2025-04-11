package com.example.Kalendar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private TextView textTime, textDayMonth, textYear;
    private final Handler timeHandler = new Handler();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd MMMM", new Locale("ru"));
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            textTime.setText(timeFormat.format(calendar.getTime()));
            textDayMonth.setText(dayMonthFormat.format(calendar.getTime()));
            textYear.setText(yearFormat.format(calendar.getTime()));
            timeHandler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textTime = view.findViewById(R.id.textTime);
        textDayMonth = view.findViewById(R.id.textDayMonth);
        textYear = view.findViewById(R.id.textYear);

        updateTimeRunnable.run();

        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getTodayTasks());
        tasksRecyclerView.setAdapter(taskAdapter);

        return view;
    }

    private List<Task> getTodayTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("08:00", "Завтрак", "Плотный завтрак", false));
        tasks.add(new Task("10:00", "Прогулка", "Прогулка с собакой", true));
        tasks.add(new Task("12:00", "Встреча", "Встреча с другом", false));
        tasks.add(new Task("13:00", "Обед", "Вкусный обед", true));
        tasks.add(new Task("14:00", "Пробежка", "Пробежка на улице", false));
        tasks.add(new Task("17:00", "Занятие", "Поход в Станкин", false));
        tasks.add(new Task("20:00", "Ужин", "Моя фантазия закончилась", false));
        return tasks;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(updateTimeRunnable);
    }
}