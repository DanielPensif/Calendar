package com.example.Kalendar.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.AddTaskDialogFragment;
import com.example.Kalendar.fragments.CompleteTaskDialogFragment;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.Objects;

public class TaskViewHolder extends RecyclerView.ViewHolder {

    public TaskViewHolder(View itemView) {
        super(itemView);
    }

    public static void bind(TaskEntity task, View itemView, Runnable onChange) {
        TextView title = itemView.findViewById(R.id.taskTitle);
        TextView category = itemView.findViewById(R.id.taskCategory);
        TextView comment = itemView.findViewById(R.id.taskComment);
        ImageButton btnComplete = itemView.findViewById(R.id.btnComplete);
        ImageButton btnMenu = itemView.findViewById(R.id.btnMenu);

        title.setText(task.title + (task.done ? " ✅" : ""));
        category.setText("Категория: " + task.category);

        if (task.comment != null && !task.comment.trim().isEmpty()) {
            comment.setText(task.comment);
            comment.setVisibility(View.VISIBLE);
        } else {
            comment.setVisibility(View.GONE);
        }

        btnComplete.setVisibility(task.done ? View.GONE : View.VISIBLE);
        btnMenu.setVisibility(task.done ? View.VISIBLE : View.GONE);

        btnComplete.setOnClickListener(v -> {
            CompleteTaskDialogFragment dialog = CompleteTaskDialogFragment.newInstance(task.id);
            dialog.setOnTaskCompletedListener(() -> {
                if (onChange != null) onChange.run();
            });
            dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "completeTask");
        });

        btnMenu.setOnClickListener(v -> showPopup(v, task, onChange));
    }

    public static void showPopup(View anchor, TaskEntity task, Runnable onChange) {
        PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
        menu.getMenu().add("Редактировать");
        menu.getMenu().add("Отменить выполнение");
        menu.getMenu().add("Изменить ревью");

        menu.setOnMenuItemClickListener(item -> {
            Context context = anchor.getContext();
            AppDatabase db = AppDatabase.getDatabase(context);

            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Редактировать":
                    showEditDialog(context, task, onChange);
                    break;

                case "Отменить выполнение":
                    task.done = false;
                    task.reviewComment = null;
                    task.doneReason = null;
                    task.completionDate = null;
                    task.completionTime = null;
                    task.rating = null;
                    new Thread(() -> {
                        db.taskDao().update(task);
                        if (onChange != null) onChange.run();
                    }).start();
                    break;

                case "Изменить ревью":
                    CompleteTaskDialogFragment dialog = CompleteTaskDialogFragment.newInstance(task.id);
                    dialog.setOnTaskCompletedListener(() -> {
                        if (onChange != null) onChange.run();
                    });
                    dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "editReview");
                    break;
            }

            return true;
        });

        menu.show();
    }

    public static void handleClick(Context context, TaskEntity task, Runnable onChange) {
        if (task.done) return;

        showEditDialog(context, task, onChange);
    }

    private static void showEditDialog(Context context, TaskEntity task, Runnable onChange) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            DayEntity day = db.dayDao().getById(task.dayId);
            if (day == null) return;

            LocalDate date = Instant.ofEpochMilli(day.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            ((AppCompatActivity) context).runOnUiThread(() -> {
                AddTaskDialogFragment dialog = AddTaskDialogFragment.editInstance(task.id, date);
                dialog.setOnTaskSavedListener(() -> {
                    if (onChange != null) onChange.run();
                });
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "editTask");
            });
        }).start();
    }
}
