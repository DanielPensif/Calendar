package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.adapters.CalendarAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.viewmodel.CalendarManagerViewModel;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

public class CalendarManagerActivity extends AppCompatActivity {
    private CalendarManagerViewModel vm;
    private CalendarAdapter adapter;
    private int currentUserId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_manager);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView + Adapter
        RecyclerView rv = findViewById(R.id.calendarRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarAdapter(new ArrayList<>(), this::onEdit, this::onDelete);
        rv.setAdapter(adapter);

        // ViewModel
        vm = new ViewModelProvider(this).get(CalendarManagerViewModel.class);
        currentUserId = SessionManager.getLoggedInUserId(this);
        vm.setUserId(currentUserId);
        vm.calendars.observe(this, list -> adapter.setItems(list));

        // Add button
        findViewById(R.id.addCalendarBtn).setOnClickListener(v -> showAddDialog());
    }

    private void onEdit(CalendarEntity e) {
        showCalendarDialog(e, (title, color) -> {
            e.title    = title;
            e.colorHex = color;
            vm.updateCalendar(e);
        });
    }

    private void onDelete(CalendarEntity e) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить календарь?")
                .setMessage("Это удалит все связанные данные.")
                .setPositiveButton("Удалить", (d,w) -> vm.deleteCalendar(e))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAddDialog() {
        showCalendarDialog(null, (title, color) -> {
            CalendarEntity e = new CalendarEntity(title, System.currentTimeMillis(), color, currentUserId);
            vm.createCalendar(e);
        });
    }

    @SuppressLint("InflateParams")
    private void showCalendarDialog(CalendarEntity existing, BiConsumer<String,String> onSave) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(existing == null ? "Новый календарь" : "Редактировать календарь");
        View dlgView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        EditText  titleInput = dlgView.findViewById(R.id.dialogTitle);
        Button    colorBtn   = dlgView.findViewById(R.id.dialogColor);

        String[] color = { existing != null ? existing.colorHex : "#67BA80" };
        titleInput.setText(existing != null ? existing.title : "");
        colorBtn.setBackgroundColor(Color.parseColor(color[0]));
        colorBtn.setOnClickListener(v -> {
            ColorPickerDialogBuilder.with(this)
                    .setTitle("Выберите цвет")
                    .initialColor(Color.parseColor(color[0]))
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(10)
                    .setPositiveButton("OK", (d, selColor, all) -> {
                        color[0] = String.format("#%06X", (0xFFFFFF & selColor));
                        colorBtn.setBackgroundColor(selColor);
                    })
                    .setNegativeButton("Отмена", null)
                    .build()
                    .show();
        });

        b.setView(dlgView);
        b.setPositiveButton("Сохранить", null);
        b.setNegativeButton("Отмена", null);
        AlertDialog dlg = b.create();
        dlg.show();
        dlg.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String t = titleInput.getText().toString().trim();
                    if (t.isEmpty()) {
                        titleInput.setError("Введите название");
                        return;
                    }
                    onSave.accept(t, color[0]);
                    dlg.dismiss();
                });
    }

    interface BiConsumer<A,B> { void accept(A a, B b); }
}