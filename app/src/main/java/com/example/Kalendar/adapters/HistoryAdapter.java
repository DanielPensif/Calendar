package com.example.Kalendar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Kalendar.R;
import com.example.Kalendar.models.CompletedDay;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<CompletedDay> days;
    private final OnAwardClickListener listener;

    public interface OnAwardClickListener {
        void onAwardClick(int position);
    }

    public HistoryAdapter(List<CompletedDay> days, OnAwardClickListener listener) {
        this.days = days;
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
        CompletedDay day = days.get(position);
        holder.dayText.setText(day.getDate());

        holder.itemView.setBackgroundResource(R.drawable.completed_day_background);

        holder.itemView.setOnClickListener(v -> listener.onAwardClick(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;

        ViewHolder(View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}
