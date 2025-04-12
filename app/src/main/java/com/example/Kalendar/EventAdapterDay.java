package com.example.Kalendar;

import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.recyclerview.widget.*;

import java.util.*;

public class EventAdapterDay extends RecyclerView.Adapter<EventAdapterDay.EventViewHolder> {

    private final List<Event> events;

    public EventAdapterDay(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.delete.setOnClickListener(v -> {
            events.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView delete;

        EventViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            delete = itemView.findViewById(R.id.eventDelete);
        }
    }
}
