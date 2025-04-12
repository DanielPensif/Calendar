package com.example.Kalendar;

import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class TaskAdapterDay extends RecyclerView.Adapter<TaskAdapterDay.TaskViewHolder> {

    private final List<Task> tasks;

    public TaskAdapterDay(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_simple, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.title.setText(task.getTitle());
        holder.time.setText(task.getTime());
        holder.delete.setOnClickListener(v -> {
            tasks.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;
        ImageView delete;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            time = itemView.findViewById(R.id.taskTime);
            delete = itemView.findViewById(R.id.taskDelete);
        }
    }
}
