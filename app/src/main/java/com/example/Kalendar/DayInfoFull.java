package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class DayInfoFull extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_info_full);

        TextView dateText = findViewById(R.id.dateText);
        TextView t = findViewById(R.id.tasksTextView);
        TextView e = findViewById(R.id.eventsTextView);
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
                new Task("17:00", "Занятие", "Поход в Станкин ...", false),
                new Task("20:00", "Ужин", "Моя фантазия закончилась", false),
                new Task("21:00", "Моя фантазия закончилась", "Моя фантазия закончилась", false),
                new Task("23:00", "Спать", "Моя фантазия закончилась", false)
        );
        e.setOnClickListener(v -> {
            Intent intent = new Intent(DayInfoFull.this, EventEditorActivity.class);
            intent.putParcelableArrayListExtra("events_list", new ArrayList<>(events));
            startActivity(intent);
        });
        eventsRecyclerView.setOnClickListener(v -> {
            Intent intent = new Intent(DayInfoFull.this, EventEditorActivity.class);
            intent.putParcelableArrayListExtra("events_list", new ArrayList<>(events));
            startActivity(intent);
        });
        t.setOnClickListener(v -> {
            Intent intent2 = new Intent(DayInfoFull.this, TaskEditorActivity.class);
            intent2.putParcelableArrayListExtra("tasks_list", new ArrayList<>(tasks));
            startActivity(intent2);
        });
        tasksRecyclerView.setOnClickListener(v -> {
            Intent intent2 = new Intent(DayInfoFull.this, TaskEditorActivity.class);
            intent2.putParcelableArrayListExtra("tasks_list", new ArrayList<>(tasks));
            startActivity(intent2);
        });

        eventsRecyclerView.setAdapter(new EventAdapterDay(new ArrayList<>(events)));
        tasksRecyclerView.setAdapter(new TaskAdapterDay(new ArrayList<>(tasks)));

    }
}