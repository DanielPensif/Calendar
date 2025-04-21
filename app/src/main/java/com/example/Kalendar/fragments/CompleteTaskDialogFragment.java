package com.example.Kalendar.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.LocalDate;

import java.util.Arrays;
import java.util.List;

public class CompleteTaskDialogFragment extends DialogFragment {

    private static final String ARG_TASK_ID = "task_id";
    private int taskId;

    public static CompleteTaskDialogFragment newInstance(int taskId) {
        CompleteTaskDialogFragment f = new CompleteTaskDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted();
    }

    private OnTaskCompletedListener listener;

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        taskId = requireArguments().getInt(ARG_TASK_ID);

        View view = getLayoutInflater().inflate(R.layout.dialog_complete_task, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        RadioGroup onTimeGroup = view.findViewById(R.id.onTimeGroup);
        Spinner reasonSpinner = view.findViewById(R.id.reasonSpinner);
        TextView reasonLabel = view.findViewById(R.id.reasonLabel);
        EditText commentInput = view.findViewById(R.id.commentInput);
        Button dateButton = view.findViewById(R.id.dateButton);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        final LocalDate[] selectedDate = {LocalDate.now()};
        dateButton.setText("📅 " + selectedDate[0].toString());

        dateButton.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate[0] = LocalDate.of(year, month + 1, dayOfMonth);
                        dateButton.setText("📅 " + selectedDate[0]);
                    },
                    today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
            dialog.show();
        });

        reasonSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Плохо планировал", "Прокрастинация", "Отвлекли", "Другое")));

        onTimeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isLate = (checkedId == R.id.radioNo);
            reasonLabel.setVisibility(isLate ? View.VISIBLE : View.GONE);
            reasonSpinner.setVisibility(isLate ? View.VISIBLE : View.GONE);
        });

        AppDatabase db = AppDatabase.getDatabase(requireContext());

        new Thread(() -> {
            TaskEntity task = db.taskDao().getById(taskId);
            if (task != null) {
                requireActivity().runOnUiThread(() -> {
                    if (task.reviewComment != null)
                        commentInput.setText(task.reviewComment);
                    if (task.doneReason != null) {
                        onTimeGroup.check(R.id.radioNo);
                        reasonSpinner.setVisibility(View.VISIBLE);
                        reasonLabel.setVisibility(View.VISIBLE);
                        reasonSpinner.setSelection(getReasonIndex(task.doneReason));
                    } else {
                        onTimeGroup.check(R.id.radioYes);
                    }
                    if (task.completionTime != null) {
                        timePicker.setVisibility(View.VISIBLE);
                        String[] timeParts = task.completionTime.split(":");
                        timePicker.setHour(Integer.parseInt(timeParts[0]));
                        timePicker.setMinute(Integer.parseInt(timeParts[1]));
                    }
                    if (task.completionDate != null) {
                        selectedDate[0] = LocalDate.parse(task.completionDate);
                        dateButton.setText("📅 " + selectedDate[0].toString());
                    }
                    if (task.rating != null) {
                        ratingBar.setRating(task.rating);
                    }
                });
            }
        }).start();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Завершение задачи");
        builder.setView(view);
        builder.setPositiveButton("Сохранить", null);  // мы перехватим вручную
        builder.setNegativeButton("Отмена", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                boolean onTime = onTimeGroup.getCheckedRadioButtonId() == R.id.radioYes;
                String reason = onTime ? null : reasonSpinner.getSelectedItem().toString();
                String comment = commentInput.getText().toString().trim();
                String time = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                int rating = (int) ratingBar.getRating();

                if (!onTime && reason.trim().isEmpty()) {
                    Toast.makeText(requireContext(), "Укажите причину задержки", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rating == 0) {
                    Toast.makeText(requireContext(), "Поставьте оценку выполнения задачи", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    TaskEntity task = db.taskDao().getById(taskId);
                    if (task != null) {
                        task.done = true;
                        task.doneReason = reason;
                        task.reviewComment = comment;
                        task.completionDate = selectedDate[0].toString();
                        task.completionTime = time;
                        task.rating = rating;
                        db.taskDao().update(task);
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (listener != null) listener.onTaskCompleted();
                        dialog.dismiss();
                    });
                }).start();
            });
        });

        return dialog;
    }
    private int getReasonIndex(String reason) {
        List<String> reasons = Arrays.asList("Плохо планировал", "Прокрастинация", "Отвлекли", "Другое");
        return reasons.indexOf(reason);
    }
}


