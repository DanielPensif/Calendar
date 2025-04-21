package com.example.Kalendar.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.fragments.AddEventDialogFragment;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

public class EventViewHolder extends RecyclerView.ViewHolder {

    private final TextView title, time, category, description, seriesInfo;

    public EventViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.eventTitle);
        time = itemView.findViewById(R.id.eventTime);
        category = itemView.findViewById(R.id.eventCategory);
        description = itemView.findViewById(R.id.eventDescription);
        seriesInfo = itemView.findViewById(R.id.eventSeriesInfo);
    }

    public void bind(EventEntity event) {
        title.setText(event.title);
        time.setText(event.allDay ? "Весь день" : event.timeStart + " - " + event.timeEnd);
        category.setText("Категория: " + event.category);
        description.setText(event.description != null ? event.description : "");

        if (event.repeatRule != null && !event.repeatRule.isEmpty()) {
            seriesInfo.setVisibility(View.VISIBLE);
            seriesInfo.setText("Повторяющееся событие");
        } else {
            seriesInfo.setVisibility(View.GONE);
        }
    }

    public static void handleClick(View view, EventEntity event, Runnable onChange) {
        Context context = view.getContext();
        if (!(context instanceof AppCompatActivity)) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);

            EventEntity actual = event;

            if (event.id == 0 && event.repeatRule != null) {
                actual = db.eventDao().getByDayId(event.dayId).stream()
                        .filter(e -> e.repeatRule != null && !e.repeatRule.isEmpty())
                        .findFirst()
                        .orElse(null);
            }

            if (actual == null) return;

            LocalDate date = (event.date != null && !event.date.isEmpty())
                    ? LocalDate.parse(event.date)
                    : null;

            if (date == null) {
                DayEntity day = db.dayDao().getById(actual.dayId);
                if (day != null) {
                    date = Instant.ofEpochMilli(day.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                }
            }

            if (date == null) return;

            LocalDate finalDate = date;
            EventEntity finalActual = actual;

            ((AppCompatActivity) context).runOnUiThread(() -> {
                AddEventDialogFragment dialog = AddEventDialogFragment.editInstance(finalActual.id, finalDate);
                dialog.setOnEventSavedListener(() -> {
                    if (onChange != null) onChange.run();
                });
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "editEvent");
            });
        }).start();
    }
    }
