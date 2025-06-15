package com.example.Kalendar.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Kalendar.R;
import com.example.Kalendar.models.CalendarEntity;

import java.util.List;
import java.util.function.Consumer;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private final List<CalendarEntity> list;
    private final Consumer<CalendarEntity> onEdit;
    private final Consumer<CalendarEntity> onDelete;

    public CalendarAdapter(List<CalendarEntity> list,
                           Consumer<CalendarEntity> onEdit,
                           Consumer<CalendarEntity> onDelete) {
        this.list = list;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    /** Позволяет обновить данные извне */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<CalendarEntity> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_row, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        CalendarEntity cal = list.get(pos);
        h.bind(cal);
    }

    @Override public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        View colorPreview;
        ImageButton editBtn, deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            title        = itemView.findViewById(R.id.calendarNameInput);
            colorPreview = itemView.findViewById(R.id.colorPreview);
            editBtn      = itemView.findViewById(R.id.btnEdit);
            deleteBtn    = itemView.findViewById(R.id.deleteBtn);
        }
        void bind(CalendarEntity cal) {
            title.setText(cal.title);
            colorPreview.setBackgroundColor(Color.parseColor(cal.colorHex));

            editBtn.setOnClickListener(v -> onEdit.accept(cal));
            deleteBtn.setOnClickListener(v -> onDelete.accept(cal));
        }
    }
}