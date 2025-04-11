package com.example.Kalendar;

import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.timeText.setText(task.time);
        holder.titleText.setText(task.title);
        holder.descriptionText.setText(task.description);
        holder.doneButton.setImageResource(task.isDone ? R.drawable.ic_check_done : R.drawable.ic_check);
        holder.doneButton.setOnClickListener(v -> {
            task.isDone = !task.isDone;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, titleText, descriptionText;
        ImageView deleteButton, doneButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.taskTime);
            titleText = itemView.findViewById(R.id.taskTitle);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            deleteButton = itemView.findViewById(R.id.taskDelete);
            doneButton = itemView.findViewById(R.id.taskDone);
        }
    }
}

