package com.example.Kalendar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class EventEditorActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private Button addEventButton;
    private EditText titleInput, descInput, timeInput;
    private CheckBox notifyCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_editor);

        ArrayList<Event> events = getIntent(). getParcelableArrayListExtra("events_list");
        recyclerView = findViewById(R.id.eventEditorList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(getSampleTasks()));

        addEventButton = findViewById(R.id.addEventButton);
        titleInput = findViewById(R.id.eventTitleInput);
        descInput = findViewById(R.id.eventDescInput);
        timeInput = findViewById(R.id.eventTimeInput);
        notifyCheck = findViewById(R.id.eventNotifyCheck);

        addEventButton.setOnClickListener(v -> {
            // пока не реализуем логику добавления
            Toast.makeText(this, "Задача добавлена (визуально)", Toast.LENGTH_SHORT).show();
        });
    }

    private List<Task> getSampleTasks() {
        return Arrays.asList(
                new Task("08:00", "Задача 1", "Описание", false),
                new Task("10:00", "Задача 2", "Описание", true)
        );
    }
}