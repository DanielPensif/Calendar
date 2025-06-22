package com.example.Kalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.fragments.AddEventDialogFragment;
import com.example.Kalendar.fragments.EventsFragment;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.db.AppDatabase;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private final Context ctx;
    private final List<EventEntity> events;

    public EventAdapter(Context ctx, List<EventEntity> list) {
        this.ctx = ctx;
        this.events = list;
    }

    public void updateEvents(List<EventEntity> newList) {
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return events.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return events.get(o).id == newList.get(n).id;
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                EventEntity a = events.get(o), b = newList.get(n);
                return a.title.equals(b.title)
                        && a.timeStart.equals(b.timeStart)
                        && a.timeEnd.equals(b.timeEnd)
                        && a.allDay == b.allDay;
            }
        });
        events.clear();
        events.addAll(newList);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new ViewHolder(
                LayoutInflater.from(p.getContext())
                        .inflate(R.layout.item_event, p, false)
        );
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        h.bind(events.get(pos));
    }

    @Override public int getItemCount() { return events.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvCat, tvDesc;

        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.eventTitle);
            tvTime  = v.findViewById(R.id.eventTime);
            tvCat   = v.findViewById(R.id.eventCategory);
            tvDesc  = v.findViewById(R.id.eventDescription);
        }

        void bind(EventEntity e) {
            tvTitle.setText(e.title);
            tvCat.setText("Категория: " + e.category);
            if (e.allDay) tvTime.setText("Весь день");
            else tvTime.setText(e.timeStart + " – " + e.timeEnd);

            if (e.description != null && !e.description.trim().isEmpty()) {
                tvDesc.setText(e.description);
                tvDesc.setVisibility(View.VISIBLE);
            } else {
                tvDesc.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                long ts = AppDatabase.getDatabase(ctx)
                        .dayDao()
                        .getById(e.dayId)
                        .getTimestamp();
                LocalDate date = Instant.ofEpochMilli(ts)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                AddEventDialogFragment dlg = AddEventDialogFragment.editInstance(e.id, date);
                dlg.setOnEventSavedListener(() ->
                        ((EventsFragment)((AppCompatActivity)ctx)
                                .getSupportFragmentManager()
                                .findFragmentByTag("f1"))
                                .refresh()
                );
                dlg.show(((AppCompatActivity)ctx).getSupportFragmentManager(), "editEvent");
            });
        }
    }
}