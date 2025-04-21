package com.example.Kalendar.adapters;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.AddTaskDialogFragment;
import com.example.Kalendar.fragments.CompleteTaskDialogFragment;
import com.example.Kalendar.fragments.TasksFragment;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.TaskEntity;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.*;
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private final List<TaskEntity> list;

    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;

    private final OnTaskChangedListener listener;

    public TaskAdapter(List<TaskEntity> tasks, OnTaskChangedListener listener) {
        this.list = tasks;
        this.listener = listener;
    }

    public interface OnTaskChangedListener {
        void onTaskChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, comment;

        ImageButton btnComplete, btnMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            category = itemView.findViewById(R.id.taskCategory);
            comment = itemView.findViewById(R.id.taskComment);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(TaskEntity task) {
            if (task == null) return; // заголовок

            title.setText(task.title + (task.done ? " ✅" : ""));
            category.setText("Категория: " + task.category);

            if (task.comment != null && !task.comment.trim().isEmpty()) {
                comment.setText(task.comment);
                comment.setVisibility(View.VISIBLE);
            } else {
                comment.setVisibility(View.GONE);
            }

            if (task.done) {
                itemView.setBackgroundColor(Color.parseColor("#E0E0E0"));
                btnComplete.setVisibility(View.GONE);
                btnMenu.setVisibility(View.VISIBLE);

                btnMenu.setOnClickListener(v -> {
                    PopupMenu menu = new PopupMenu(v.getContext(), btnMenu);
                    menu.getMenu().add("Редактировать задачу");
                    menu.getMenu().add("Отменить выполнение");
                    menu.getMenu().add("Изменить ревью");



                    menu.setOnMenuItemClickListener(item -> {
                        switch (Objects.requireNonNull(item.getTitle()).toString()) {
                            case "Редактировать задачу":
                                new Thread(() -> {
                                    if (listener != null) listener.onTaskChanged();
                                    AppDatabase db = AppDatabase.getDatabase(v.getContext());
                                    DayEntity day = db.dayDao().getById(task.dayId);

                                    if (day != null) {
                                        LocalDate date = Instant.ofEpochMilli(day.timestamp)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate();

                                        ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                                            AddTaskDialogFragment editDialog = AddTaskDialogFragment.editInstance(task.id, date);
                                            editDialog.setOnTaskSavedListener(() -> {
                                                Fragment f = ((AppCompatActivity) v.getContext())
                                                        .getSupportFragmentManager()
                                                        .findFragmentByTag("f0");
                                                if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                                            });
                                            editDialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editTask");
                                        });
                                    }
                                }).start();
                                return true;


                            case "Отменить выполнение":
                                task.done = false;
                                task.doneReason = null;
                                task.completionDate = null;
                                task.completionTime = null;
                                task.reviewComment = null;
                                task.rating = null;
                                new Thread(() -> {
                                    AppDatabase.getDatabase(v.getContext()).taskDao().update(task);
                                    if (listener != null) listener.onTaskChanged();
                                    if (v.getContext() instanceof AppCompatActivity) {
                                        Fragment f = ((AppCompatActivity) v.getContext())
                                                .getSupportFragmentManager()
                                                .findFragmentByTag("f0");
                                        if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                                    }
                                }).start();

                                return true;
                            case "Изменить ревью":
                                CompleteTaskDialogFragment dialog = CompleteTaskDialogFragment.newInstance(task.id);
                                dialog.setOnTaskCompletedListener(() -> {
                                    if (listener != null) listener.onTaskChanged();
                                    if (v.getContext() instanceof AppCompatActivity) {
                                        Fragment f = ((AppCompatActivity) v.getContext())
                                                .getSupportFragmentManager()
                                                .findFragmentByTag("f0");
                                        if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                                    }
                                });
                                dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editReview");
                                return true;
                        }
                        return false;
                    });
                    menu.show();
                });

            } else {
                itemView.setBackgroundColor(Color.WHITE);
                btnComplete.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.GONE);

                btnComplete.setOnClickListener(v -> {
                    CompleteTaskDialogFragment dialog = CompleteTaskDialogFragment.newInstance(task.id);
                    dialog.setOnTaskCompletedListener(() -> {
                        if (listener != null) listener.onTaskChanged();
                        if (v.getContext() instanceof AppCompatActivity) {
                            Fragment f = ((AppCompatActivity) v.getContext())
                                    .getSupportFragmentManager()
                                    .findFragmentByTag("f0");
                            if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                        }
                    });
                    dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "completeTask");
                });
            }

            if (!task.done) {
                itemView.setOnClickListener(v -> new Thread(() -> {
                    AppDatabase db = AppDatabase.getDatabase(v.getContext());
                    DayEntity day = db.dayDao().getById(task.dayId);

                    if (day != null) {
                        LocalDate date = Instant.ofEpochMilli(day.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        ((AppCompatActivity) v.getContext()).runOnUiThread(() -> {
                            AddTaskDialogFragment dialog = AddTaskDialogFragment.editInstance(task.id, date);
                            dialog.setOnTaskSavedListener(() -> {
                                if (v.getContext() instanceof AppCompatActivity) {
                                    Fragment f = ((AppCompatActivity) v.getContext())
                                            .getSupportFragmentManager()
                                            .findFragmentByTag("f0");
                                    if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                                }
                            });
                            dialog.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "editTask");
                        });
                    }
                }).start());
            } else {
                itemView.setOnClickListener(null);
            }

            itemView.setOnLongClickListener(v -> {
                PopupMenu menu = new PopupMenu(v.getContext(), v);
                menu.getMenu().add("Дублировать");
                menu.getMenu().add("Удалить");

                menu.setOnMenuItemClickListener(item -> switch (Objects.requireNonNull(item.getTitle()).toString()) {
                    case "Дублировать" -> {
                        new Thread(() -> {
                            AppDatabase db = AppDatabase.getDatabase(v.getContext());

                            TaskEntity duplicate = new TaskEntity();
                            duplicate.title = task.title + " (копия)";
                            duplicate.comment = task.comment;
                            duplicate.category = task.category;
                            duplicate.calendarId = task.calendarId;
                            duplicate.dayId = task.dayId;
                            duplicate.done = false;

                            db.taskDao().insert(duplicate);

                            if (v.getContext() instanceof AppCompatActivity) {
                                Fragment f = ((AppCompatActivity) v.getContext())
                                        .getSupportFragmentManager()
                                        .findFragmentByTag("f0");
                                if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                            }
                        }).start();
                        yield true;
                    }
                    case "Удалить" -> {
                        new Thread(() -> {
                            AppDatabase db = AppDatabase.getDatabase(v.getContext());
                            db.taskDao().delete(task);

                            if (v.getContext() instanceof AppCompatActivity) {
                                Fragment f = ((AppCompatActivity) v.getContext())
                                        .getSupportFragmentManager()
                                        .findFragmentByTag("f0");
                                if (f instanceof TasksFragment) ((TasksFragment) f).refresh();
                            }
                        }).start();
                        yield true;
                    }
                    default -> false;
                });

                menu.show();
                return true;
            });


        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View headerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task_section_header, parent, false);
            return new ViewHolder(headerView);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            return new ViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        TaskEntity t = list.get(position);
        return t == null ? TYPE_HEADER : TYPE_TASK;
    }

}

