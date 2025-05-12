package com.example.Kalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.Kalendar.R;
import com.example.Kalendar.models.CalendarEntity;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CalendarSpinnerAdapter extends ArrayAdapter<CalendarEntity> {

    private Context context;
    private List<CalendarEntity> calendars;

    public CalendarSpinnerAdapter(@NonNull Context context, List<CalendarEntity> calendars) {
        super(context, 0, calendars);
        this.context = context;
        this.calendars = calendars;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false);
        TextView text = view.findViewById(R.id.text);
        View colorCircle = view.findViewById(R.id.colorCircle);

        CalendarEntity calendar = calendars.get(position);
        if (calendar != null) {
            text.setText(calendar.title);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(calendar.colorHex));
            colorCircle.setBackground(drawable);
        } else {
            text.setText("Все календари");
            colorCircle.setBackgroundResource(R.drawable.circle_rainbow);
        }

        return view;
    }
}
