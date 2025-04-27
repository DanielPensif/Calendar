package com.example.Kalendar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.HistoryItem;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> items;
    private final OnAwardClickListener listener;

    public interface OnAwardClickListener {
        void onAwardClick(int position);
    }

    public HistoryAdapter(List<HistoryItem> items, OnAwardClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_completed_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.dateText.setText(item.dateFormatted);
        holder.calendarText.setText(item.calendarName);

        holder.itemView.setBackgroundResource(R.drawable.completed_day_background);

        holder.itemView.setOnClickListener(v -> listener.onAwardClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, calendarText;

        ViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dayText);
            calendarText = itemView.findViewById(R.id.calendarText);
        }
    }
}
