package com.example.Kalendar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddTaskDialogFragment extends BottomSheetDialogFragment {

    private LocalDate date;
    private AppDatabase db;

    private EditText inputTitle, inputComment;
    private Spinner spinnerCategory, spinnerCalendar;
    private OnTaskSavedListener listener;

    private final List<CalendarEntity> calendarEntities = new ArrayList<>();

    private Integer editingTaskId = null;


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
        TextView btnSave = view.findViewById(R.id.btnSaveTask);
        btnSave.setOnClickListener(v -> saveTask());
        inputTitle = view.findViewById(R.id.inputTaskTitle);
        inputComment = view.findViewById(R.id.inputTaskComment);
        spinnerCategory = view.findViewById(R.id.spinnerTaskCategory);
        spinnerCalendar = view.findViewById(R.id.spinnerTaskCalendar);

        setupSpinners();

        if (getArguments() != null && getArguments().containsKey("editTaskId")) {
            editingTaskId = getArguments().getInt("editTaskId");
            new Thread(() -> {
                TaskEntity task = db.taskDao().getById(editingTaskId);
                requireActivity().runOnUiThread(() -> {
                    if (task != null) {
                        inputTitle.setText(task.title);
                        inputComment.setText(task.comment);
                        spinnerCategory.setSelection(getIndex(spinnerCategory, task.category));
                    }
                });
            }).start();
        }



        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Учёба", "Работа", "Быт", "Личное"));
        spinnerCategory.setAdapter(categoryAdapter);

        new Thread(() -> {
            List<CalendarEntity> calendars = db.calendarDao().getAll();
            List<String> titles = new ArrayList<>();
            for (CalendarEntity cal : calendars) titles.add(cal.title);
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                calendarEntities.clear();
                calendarEntities.addAll(calendars); // сохраняем реальные ID
                ArrayAdapter<String> calendarAdapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item, titles);
                spinnerCalendar.setAdapter(calendarAdapter);
            });
        }).start();
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    public void saveTask() {
        String title = inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Введите название", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spinnerCategory.getSelectedItem().toString();
        String comment = inputComment.getText().toString();
        int index = spinnerCalendar.getSelectedItemPosition();
        int calendarId = calendarEntities.get(index).id;

        new Thread(() -> {
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

            if (editingTaskId != null) {
                db.taskDao().update(task);
            } else {
                db.taskDao().insert(task);
            }

            requireActivity().runOnUiThread(() -> {
                if (listener != null) listener.onTaskSaved();
                dismiss();
            });
        }).start();

    }
}
