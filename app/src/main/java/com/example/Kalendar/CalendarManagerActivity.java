package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar; // Добавлен импорт
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.google.android.material.button.MaterialButton;

import java.util.*;

public class CalendarManagerActivity extends AppCompatActivity {

    private AppDatabase db;
    private CalendarAdapter adapter;
    private final List<CalendarEntity> calendarList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_manager);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Добавляем Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = AppDatabase.getDatabase(this);

        RecyclerView recyclerView = findViewById(R.id.calendarRecycler);
        MaterialButton addCalendarBtn = findViewById(R.id.addCalendarBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarAdapter(calendarList);
        recyclerView.setAdapter(adapter);

        loadCalendars();

        addCalendarBtn.setOnClickListener(v -> showAddDialog());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCalendars() {
        new Thread(() -> {
            List<CalendarEntity> all = db.calendarDao().getAll();
            calendarList.clear();
            calendarList.addAll(all);
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новый календарь");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        EditText input = new EditText(this);
        input.setHint("Название");

        Button colorPickerBtn = new Button(this);
        colorPickerBtn.setText("Выбрать цвет");
        final String[] selectedColor = {"#67BA80"}; // по умолчанию
        colorPickerBtn.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(v.getContext())
                    .setTitle("Выберите цвет")
                    .initialColor(Color.parseColor(selectedColor[0]))
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("Сохранить", (dialog, selectedColorInt, allColors) -> {
                        selectedColor[0] = String.format("#%06X", (0xFFFFFF & selectedColorInt));
                        colorPickerBtn.setBackgroundColor(selectedColorInt);
                    })
                    .setNegativeButton("Отмена", null)
                    .build()
                    .show();
        });

        layout.addView(input);
        builder.setView(layout);
        layout.addView(colorPickerBtn);

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String title = input.getText().toString().trim();
            CalendarEntity entity = new CalendarEntity(title, System.currentTimeMillis(), selectedColor[0]);


            new Thread(() -> {
                db.calendarDao().insert(entity);
                runOnUiThread(this::loadCalendars);
                Intent intent = new Intent();
                intent.putExtra("calendarUpdated", true);
                setResult(RESULT_OK, intent);

            }).start();
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

        private final List<CalendarEntity> list;

        public CalendarAdapter(List<CalendarEntity> list) {
            this.list = list;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            View colorView;
            EditText titleInput;
            ImageButton deleteBtn;

            public ViewHolder(View itemView) {
                super(itemView);
                colorView = itemView.findViewById(R.id.colorPreview);
                titleInput = itemView.findViewById(R.id.calendarNameInput);
                deleteBtn = itemView.findViewById(R.id.deleteBtn);
            }

            public void bind(CalendarEntity calendar) {
                colorView.setBackgroundColor(Color.parseColor(calendar.colorHex));

                // Удалим предыдущий TextWatcher
                if (titleInput.getTag() instanceof TextWatcher) {
                    titleInput.removeTextChangedListener((TextWatcher) titleInput.getTag());
                }

                // Установим новое значение без вызова TextWatcher
                if (!titleInput.getText().toString().equals(calendar.title)) {
                    titleInput.setText(calendar.title);
                }

                TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String newTitle = s.toString().trim();
                        if (!newTitle.equals(calendar.title)) {
                            calendar.title = newTitle;
                            new Thread(() -> db.calendarDao().update(calendar)).start();
                        }
                    }
                };

                titleInput.addTextChangedListener(watcher);
                titleInput.setTag(watcher);

                colorView.setOnClickListener(v -> showColorEditDialog(calendar));

                deleteBtn.setOnClickListener(v -> {
                    if (calendarList.size() <= 1) {
                        Toast.makeText(CalendarManagerActivity.this, "Нельзя удалить последний календарь", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(CalendarManagerActivity.this)
                            .setTitle("Удалить календарь?")
                            .setMessage("Это удалит все связанные с ним данные.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                new Thread(() -> {
                                    db.calendarDao().delete(calendar);
                                    runOnUiThread(() -> {
                                        calendarList.remove(calendar);
                                        adapter.notifyDataSetChanged();
                                    });
                                }).start();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
            }
        }


        @NonNull
        @Override
        public CalendarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarAdapter.ViewHolder holder, int position) {
            holder.bind(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private void showColorEditDialog(CalendarEntity calendar) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Выберите цвет")
                .initialColor(Color.parseColor(calendar.colorHex)) // текущее значение
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(10) // сглаженность градиента
                .setPositiveButton("Сохранить", (dialog, selectedColor, allColors) -> {
                    calendar.colorHex = String.format("#%06X", (0xFFFFFF & selectedColor));
                    new Thread(() -> {
                        db.calendarDao().update(calendar);
                        runOnUiThread(() -> {
                            int index = calendarList.indexOf(calendar);
                            if (index != -1) adapter.notifyItemChanged(index);
                        });
                    }).start();

                    loadCalendars();
                })
                .setNegativeButton("Отмена", null)
                .build()
                .show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCalendars();
    }
}