package com.example.Kalendar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.ui.EventViewHolder;
import com.example.Kalendar.ui.TaskViewHolder;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<HomeItem> items;
    private final Context context;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EVENT = 1;
    private static final int TYPE_TASK = 2;

    private final Runnable onChange;


    public HomeAdapter(List<HomeItem> items, Context context, Runnable onChange) {
        this.items = items;
        this.context = context;
        this.onChange = onChange;
    }

    @Override
    public int getItemViewType(int position) {
        HomeItem item = items.get(position);
        if (item instanceof HomeItem.Header) return TYPE_HEADER;
        if (item instanceof HomeItem.EventItem) return TYPE_EVENT;
        return TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            TextView header = new TextView(context);
            header.setTextSize(18f);
            header.setPadding(8, 16, 8, 8);
            return new HeaderViewHolder(header);
        } else if (viewType == TYPE_EVENT) {
            View view = inflater.inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).text.setText(((HomeItem.Header) item).title);
        } else if (holder instanceof EventViewHolder) {
            EventEntity e = ((HomeItem.EventItem) item).event;
            ((EventViewHolder) holder).bind(e);

            holder.itemView.setOnClickListener(v -> EventViewHolder.handleClick(v, e, onChange));
        } else if (holder instanceof TaskViewHolder) {
            TaskEntity t = ((HomeItem.TaskItem) item).task;
            TaskViewHolder.bind(t, holder.itemView, onChange);

            holder.itemView.setOnClickListener(v -> TaskViewHolder.handleClick(v.getContext(), t, onChange));
            TaskViewHolder.bind(t, holder.itemView, onChange);
        }
    }

    public void setItems(List<HomeItem> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.text = (TextView) itemView;
        }
    }
}
