package com.example.Kalendar.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.TaskReminderReceiver;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;

public class AddTaskDialogFragment extends BottomSheetDialogFragment {

    private LocalDate date;
    private AppDatabase db;

    private EditText inputTitle, inputComment;
    private Spinner spinnerCategory, spinnerCalendar;
    private CheckBox checkReminder;
    private TimePicker timePickerReminder;
    private OnTaskSavedListener listener;
    private final List<CalendarEntity> calendarEntities = new ArrayList<>();
    private Integer editingTaskId = null;
    private int currentUserId;

    public interface OnTaskSavedListener {
        void onTaskSaved();
    }

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    public static AddTaskDialogFragment newInstance(LocalDate date) {
        AddTaskDialogFragment f = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putString("date", date.toString());
        f.setArguments(args);
        return f;
    }

    public static AddTaskDialogFragment editInstance(int taskId, LocalDate date) {
        AddTaskDialogFragment f = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putInt("editTaskId", taskId);
        args.putString("date", date.toString());
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);
        date = LocalDate.parse(Objects.requireNonNull(requireArguments().getString("date")));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        inputTitle = view.findViewById(R.id.inputTaskTitle);
        inputComment = view.findViewById(R.id.inputTaskComment);
        spinnerCategory = view.findViewById(R.id.spinnerTaskCategory);
        spinnerCalendar = view.findViewById(R.id.spinnerTaskCalendar);
        checkReminder = view.findViewById(R.id.checkReminder);
        timePickerReminder = view.findViewById(R.id.timePickerReminder);
        timePickerReminder.setIs24HourView(true);

        // показываем/скрываем TimePicker при чекбоксе
        checkReminder.setOnCheckedChangeListener((btn, checked) ->
                timePickerReminder.setVisibility(checked ? View.VISIBLE : View.GONE)
        );

        view.findViewById(R.id.btnSaveTask).setOnClickListener(v -> saveTask());
        setupSpinners();

        // если режим редактирования — загрузим задачу
        if (getArguments() != null && getArguments().containsKey("editTaskId")) {
            editingTaskId = getArguments().getInt("editTaskId");
            new Thread(() -> {
                TaskEntity task = db.taskDao().getById(editingTaskId);
                requireActivity().runOnUiThread(() -> {
                    if (task != null) {
                        inputTitle.setText(task.title);
                        inputComment.setText(task.comment);
                        spinnerCategory.setSelection(getIndex(spinnerCategory, task.category));
                        checkReminder.setChecked(task.reminderEnabled);
                        timePickerReminder.setHour(task.reminderHour);
                        timePickerReminder.setMinute(task.reminderMinute);
                        timePickerReminder.setVisibility(task.reminderEnabled ? View.VISIBLE : View.GONE);
                    }
                });
            }).start();
        }

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Учёба", "Работа", "Быт", "Личное")
        );
        spinnerCategory.setAdapter(categoryAdapter);

        new Thread(() -> {
            List<CalendarEntity> calendars = db.calendarDao().getByUserId(currentUserId);

            List<String> titles = new ArrayList<>();
            for (CalendarEntity cal : calendars) titles.add(cal.title);

            requireActivity().runOnUiThread(() -> {
                calendarEntities.clear();
                calendarEntities.addAll(calendars);
                spinnerCalendar.setAdapter(
                        new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_dropdown_item,
                                titles)
                );
            });
        }).start();
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value))
                return i;
        }
        return 0;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void saveTask() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = spinnerCategory.getSelectedItem().toString();
        String comment = inputComment.getText().toString();
        int calendarId = calendarEntities.get(spinnerCalendar.getSelectedItemPosition()).id;

        // Сохраняем все параметры напоминания, чтобы передать их в планировщик
        boolean remind = checkReminder.isChecked();
        int remHour   = timePickerReminder.getHour();
        int remMinute = timePickerReminder.getMinute();

        new Thread(() -> {
            // 1) Сохраняем задачу в БД
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(timestamp, calendarId);
            if (day == null) {
                day = new DayEntity();
                day.timestamp = timestamp;
                day.calendarId = calendarId;
                day.id = (int) db.dayDao().insert(day);
            }

            TaskEntity task;
            if (editingTaskId != null) {
                task = db.taskDao().getById(editingTaskId);
                if (task == null) return;
            } else {
                task = new TaskEntity();
                task.done = false;
            }

            task.title = title;
            task.comment = comment;
            task.category = category;
            task.dayId = day.id;
            task.calendarId = calendarId;
            task.reminderEnabled = remind;
            task.reminderHour = remHour;
            task.reminderMinute = remMinute;

            if (editingTaskId != null) {
                db.taskDao().update(task);
                checkReminder.setEnabled(false);
                timePickerReminder.setEnabled(false);
            } else {
                task.id = (int) db.taskDao().insert(task);
            }

            // 2) Обновляем UI (закрытие диалога и обновление списка) — обязательно в UI-потоке
            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onTaskSaved();
                dismiss();
            });

            // 3) И только ПОСЛЕ этого — планируем уведомление (не в UI-потоке)
            if (remind) {
                try {
                    scheduleTaskNotification(
                            requireContext(),
                            task.id,
                            "Напоминание о задаче! '" + title + "'",
                            comment,
                            remHour,
                            remMinute,
                            date.atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
);
                } catch (Exception e) {
                    // на всякий случай не мешаем пользовательскому потоку
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleTaskNotification(Context ctx,
                                          int requestCode,
                                          String title,
                                          String text,
                                          int hour,
                                          int minute,
                                          long dateMs) {
            Intent intent = new Intent(ctx, TaskReminderReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("text",  text);
            intent.putExtra("requestCode", requestCode);

            PendingIntent pi = PendingIntent.getBroadcast(
                    ctx, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 1) Создаём календарь на нужный день:
            Calendar trig = Calendar.getInstance();
            trig.setTimeInMillis(dateMs);
            trig.set(Calendar.HOUR_OF_DAY, hour);
            trig.set(Calendar.MINUTE, minute);
            trig.set(Calendar.SECOND, 0);

            if (trig.before(Calendar.getInstance())) {
                return;
            }

            AlarmManager am = ctx.getSystemService(AlarmManager.class);
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trig.getTimeInMillis(), pi);
        }

    }
