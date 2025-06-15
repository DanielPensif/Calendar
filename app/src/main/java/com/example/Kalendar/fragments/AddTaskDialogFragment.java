package com.example.Kalendar.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.CalendarSpinnerAdapter;
import com.example.Kalendar.adapters.CategorySpinnerAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.adapters.TaskReminderReceiver;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;

public class AddTaskDialogFragment extends BottomSheetDialogFragment {
    private LocalDate date;
    private AppDatabase db;
    private int currentUserId;

    private EditText inputTitle, inputComment;
    private Spinner spinnerCategory, spinnerCalendar;
    private ImageButton btnAddCategory;
    private CheckBox checkReminder;
    private TimePicker timePickerReminder;
    private Button btnSave;

    private CategorySpinnerAdapter categoryAdapter;
    private final List<CategoryEntity> categories = new ArrayList<>();
    private CalendarSpinnerAdapter calendarAdapter;
    private final List<com.example.Kalendar.models.CalendarEntity> calendars = new ArrayList<>();

    private Integer editingTaskId = null;
    private OnTaskSavedListener listener;

    public interface OnTaskSavedListener { void onTaskSaved(); }
    public void setOnTaskSavedListener(OnTaskSavedListener l) { listener = l; }

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

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_task, container, false);
        date = LocalDate.parse(requireArguments().getString("date"));
        db = AppDatabase.getDatabase(requireContext());
        currentUserId = SessionManager.getLoggedInUserId(requireContext());

        inputTitle = v.findViewById(R.id.inputTaskTitle);
        inputComment = v.findViewById(R.id.inputTaskComment);
        spinnerCategory = v.findViewById(R.id.spinnerTaskCategory);
        btnAddCategory = v.findViewById(R.id.btnAddCategory);
        spinnerCalendar = v.findViewById(R.id.spinnerTaskCalendar);
        checkReminder = v.findViewById(R.id.checkReminder);
        timePickerReminder = v.findViewById(R.id.timePickerReminder);
        timePickerReminder.setIs24HourView(true);
        btnSave = v.findViewById(R.id.btnSaveTask);

        checkReminder.setOnCheckedChangeListener((b, c) ->
                timePickerReminder.setVisibility(c ? View.VISIBLE : View.GONE)
        );
        btnSave.setOnClickListener(x -> saveTask());

        setupCategorySpinner();
        setupCalendarSpinner();

        if (getArguments().containsKey("editTaskId")) {
            editingTaskId = getArguments().getInt("editTaskId");
            new Thread(() -> {
                TaskEntity task = db.taskDao().getById(editingTaskId);
                if (task == null) return;
                new Handler(Looper.getMainLooper()).post(() -> {
                    inputTitle.setText(task.title);
                    inputComment.setText(task.comment);
                    // выставляем категорию
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).name.equals(task.category)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                    checkReminder.setChecked(task.reminderEnabled);
                    timePickerReminder.setHour(task.reminderHour);
                    timePickerReminder.setMinute(task.reminderMinute);
                    timePickerReminder.setVisibility(task.reminderEnabled ? View.VISIBLE : View.GONE);
                    // выставляем календарь
                    for (int i = 0; i < calendars.size(); i++) {
                        if (calendars.get(i).id == task.calendarId) {
                            spinnerCalendar.setSelection(i);
                            break;
                        }
                    }
                });
            }).start();
        }

        return v;
    }

    private void setupCategorySpinner() {
        categoryAdapter = new CategorySpinnerAdapter(
                requireContext(), categories, db, currentUserId,
                () -> categoryAdapter.notifyDataSetChanged()
        );
        spinnerCategory.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(x ->
                categoryAdapter.showCategoryDialog(null)
        );
    }

    private void setupCalendarSpinner() {
        calendarAdapter = new CalendarSpinnerAdapter(
                requireContext(), calendars
        );
        spinnerCalendar.setAdapter(calendarAdapter);
        // load
        new Thread(() -> {
            List<com.example.Kalendar.models.CalendarEntity> list =
                    db.calendarDao().getByUserId(currentUserId);
            new Handler(Looper.getMainLooper()).post(() -> {
                calendars.clear();
                calendars.addAll(list);
                calendarAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void saveTask() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }
        CategoryEntity selCat = (CategoryEntity) spinnerCategory.getSelectedItem();
        int calendarId = calendars.get(spinnerCalendar.getSelectedItemPosition()).id;
        boolean rem = checkReminder.isChecked();
        int hr = timePickerReminder.getHour(), mn = timePickerReminder.getMinute();
        String comment = inputComment.getText().toString();

        new Thread(() -> {
            long ts = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            DayEntity day = db.dayDao().getByTimestampAndCalendarId(ts, calendarId);
            if (day == null) {
                day = new DayEntity(); day.timestamp = ts; day.calendarId = calendarId;
                day.id = (int) db.dayDao().insert(day);
            }
            TaskEntity task = editingTaskId != null
                    ? db.taskDao().getById(editingTaskId)
                    : new TaskEntity();
            if (task == null) return;

            task.title = title;
            task.comment = comment;
            task.category = selCat.name;
            task.dayId = day.id;
            task.calendarId = calendarId;
            task.reminderEnabled = rem;
            task.reminderHour = hr;
            task.reminderMinute = mn;

            if (editingTaskId != null) db.taskDao().update(task);
            else task.id = (int) db.taskDao().insert(task);

            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onTaskSaved();
                dismiss();
            });

            if (rem) {
                scheduleTaskNotification(
                        requireContext(),
                        task.id,
                        "Напоминание: " + title,
                        comment,
                        hr, mn,
                        ts
                );
            }
        }).start();
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleTaskNotification(Context ctx,
                                          int code,
                                          String title,
                                          String text,
                                          int hr,
                                          int mn,
                                          long dateMs) {
        Intent i = new Intent(ctx, TaskReminderReceiver.class);
        i.putExtra("title", title);
        i.putExtra("text", text);
        i.putExtra("requestCode", code);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateMs);
        c.set(Calendar.HOUR_OF_DAY, hr);
        c.set(Calendar.MINUTE, mn);
        c.set(Calendar.SECOND, 0);
        if (c.before(Calendar.getInstance())) return;
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }
}