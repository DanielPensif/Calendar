package com.example.Kalendar;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class DayInfoFull extends AppCompatActivity {

    private RecyclerView tasksRecyclerView;
    private TaskAdapterDay taskAdapterDay;
    private TextView textDayMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_info_full);

        // TODO TODO TODO TODO TODO TODO

        // Добавить сверху число с месяцем в виде по типу: 32 января
        // Затем RecyclerView с событиями "Events" с фиолетовой обводкой прямоугольника
        // Затем RecyclerView с задачами "Tasks" с синей обводкой прямоугольника

        // TODO TODO TODO TODO TODO TODO
        TextView dateText = findViewById(R.id.dateText);
        RecyclerView eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        RecyclerView tasksRecyclerView = findViewById(R.id.tasksRecyclerView);

        String date = getIntent().getStringExtra("date");
        dateText.setText(date);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Event> events = Arrays.asList(
                new Event("Событие 1"),
                new Event("Событие 2"),
                new Event("Событие 3"),
                new Event("Событие 4"),
                new Event("Событие 5"),
                new Event("Событие 6"),
                new Event("Событие 7")
        );

        List<Task> tasks = Arrays.asList(
                new Task("08:00", "Завтрак", "Плотный завтрак", false),
                new Task("12:00", "Встреча", "Встреча с другом", false),
                new Task("13:00", "Обед", "Вкусный обед", true),
                new Task("14:00", "Пробежка", "Пробежка на улице", false),
                new Task("17:00", "Занятие", "Поход в Станкин фывфывфаывфывпыфвпаывпыпывацуфаыфвуавыапыкапеып", false),
                new Task("20:00", "Ужин", "Моя фантазия закончилась", false),
                new Task("21:00", "Моя фантазия закончилась", "Моя фантазия закончилась", false),
                new Task("23:00", "Спать", "Моя фантазия закончилась", false)
        );

        eventsRecyclerView.setAdapter(new EventAdapterDay(new ArrayList<>(events)));
        tasksRecyclerView.setAdapter(new TaskAdapterDay(new ArrayList<>(tasks)));
    }
}