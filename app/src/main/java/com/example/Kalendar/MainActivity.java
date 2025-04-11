package com.example.Kalendar;

import android.content.*;
import android.os.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.text.*;
import java.util.*;


public class MainActivity extends AppCompatActivity {

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

            // Обновляем каждую секундк
            timeHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTime = findViewById(R.id.textTime);
        textDayMonth = findViewById(R.id.textDayMonth);
        textYear = findViewById(R.id.textYear);

        updateTimeRunnable.run();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(getTodayTasks());
        tasksRecyclerView.setAdapter(taskAdapter);

        findViewById(R.id.dailyTasksTitle).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    private List<Task> getTodayTasks() {
        // Тестовые задачи, пока просто для проверки ресуслер вью
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("08:00", "Завтрак", "Плотный завтрак", false));
        tasks.add(new Task("10:00", "Прогулка", "Прогулка с собакой", true));
        tasks.add(new Task("12:00", "Встреча", "Встреча с другом", false));
        tasks.add(new Task("13:00", "Обед", "Вкусный обед", true));
        tasks.add(new Task("14:00", "Пробежка", "Пробежка на улице", false));
        tasks.add(new Task("17:00", "Занятие", "Поход на занятие в Станкин аыварыврдылрадлфыраврфылдвар", false));
        tasks.add(new Task("20:00", "Ужин", "Моя фанатазия закончилась", false));
        tasks.add(new Task("22:00", "Моя фанатазия закончилась", "Моя фанатазия закончилась", true));
        tasks.add(new Task("23:00", "Спать", "Моя фанатазия закончилась", false));
        return tasks;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(updateTimeRunnable);
    }
}