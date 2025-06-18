package com.example.Kalendar.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.fragments.AddTaskDialogFragment;
import com.example.Kalendar.fragments.CompleteTaskDialogFragment;
import com.example.Kalendar.fragments.TasksFragment;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.db.AppDatabase;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.LocalDate;

import java.util.List;
import java.util.Objects;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0, TYPE_TASK = 1;
    private final List<TaskEntity> items;
    private final OnTaskChangedListener listener;
    private final Context ctx;

    public interface OnTaskChangedListener { void onTaskChanged(); }

    public TaskAdapter(Context ctx, List<TaskEntity> items, OnTaskChangedListener l) {
        this.ctx = ctx;
        this.items = items;
        this.listener = l;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<TaskEntity> newItems) {
        items.clear();
        // вставляем null перед первым выполненным
        boolean seenDone=false;
        for (TaskEntity t: newItems) {
            if (t.done && !seenDone) {
                seenDone=true;
                items.add(null);
            }
            items.add(t);
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int pos) {
        return items.get(pos)==null ? TYPE_HEADER : TYPE_TASK;
    }

    @Override public int getItemCount() { return items.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        LayoutInflater i = LayoutInflater.from(p.getContext());
        if (vt==TYPE_HEADER) {
            return new HeaderHolder(i.inflate(R.layout.item_task_section_header,p,false));
        } else {
            return new TaskHolder(i.inflate(R.layout.item_task,p,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        if (h instanceof TaskHolder) ((TaskHolder)h).bind(items.get(pos));
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View v){ super(v); }
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCat, tvComment;
        ImageButton btnComplete, btnMenu;
        TaskHolder(View v){
            super(v);
            tvTitle=v.findViewById(R.id.taskTitle);
            tvCat=v.findViewById(R.id.taskCategory);
            tvComment=v.findViewById(R.id.taskComment);
            btnComplete=v.findViewById(R.id.btnComplete);
            btnMenu=v.findViewById(R.id.btnMenu);
        }
        void bind(TaskEntity t){
            tvTitle.setText(t.title + (t.done ? " ✅" : ""));
            tvCat.setText("Категория: " + t.category);
            if (t.comment!=null && !t.comment.isEmpty()) {
                tvComment.setText(t.comment);
                tvComment.setVisibility(View.VISIBLE);
            } else tvComment.setVisibility(View.GONE);

            if (t.done) {
                itemView.setBackgroundColor(Color.LTGRAY);
                btnComplete.setVisibility(View.GONE);
                btnMenu.setVisibility(View.VISIBLE);
                btnMenu.setOnClickListener(v -> showDoneMenu(t));
            } else {
                itemView.setBackgroundColor(Color.WHITE);
                btnComplete.setVisibility(View.VISIBLE);
                btnMenu.setVisibility(View.GONE);
                btnComplete.setOnClickListener(v -> markDone(t));
            }

            itemView.setOnClickListener(v -> {
                // редактировать
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getDatabase(v.getContext());
                    TasksFragment frag = (TasksFragment)((AppCompatActivity)ctx).getSupportFragmentManager().findFragmentByTag("f0");
                    long ts = db.dayDao().getById(t.dayId).timestamp;
                    LocalDate d = Instant.ofEpochMilli(ts)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    ((AppCompatActivity)ctx).runOnUiThread(() -> {
                        AddTaskDialogFragment dlg = AddTaskDialogFragment.editInstance(t.id,d);
                        dlg.setOnTaskSavedListener(listener::onTaskChanged);
                        dlg.show(((AppCompatActivity)ctx).getSupportFragmentManager(),"editTask");
                    });
                }).start();
            });
        }

        private void showDoneMenu(TaskEntity t){
            PopupMenu m=new PopupMenu(itemView.getContext(),btnMenu);
            m.getMenu().add("Отменить выполнение");
            m.getMenu().add("Редактировать");
            m.setOnMenuItemClickListener(it -> {
                String title=it.getTitle().toString();
                if (title.equals("Отменить выполнение")) {
                    t.done=false;
                    new Thread(() -> {
                        AppDatabase.getDatabase(ctx).taskDao().update(t);
                        listener.onTaskChanged();
                    }).start();
                } else {
                    markDone(t);
                }
                return true;
            });
            m.show();
        }

        private void markDone(TaskEntity t) {
            CompleteTaskDialogFragment dlg = CompleteTaskDialogFragment.newInstance(t.id);
            dlg.setOnTaskCompletedListener(listener::onTaskChanged);
            dlg.show(((AppCompatActivity)ctx).getSupportFragmentManager(),"complete");
        }
    }
}