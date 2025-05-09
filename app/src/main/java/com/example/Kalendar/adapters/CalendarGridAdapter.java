package com.example.Kalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.R;

import org.threeten.bp.LocalDate;
import java.util.*;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(LocalDate date);
    }
    private final Map<LocalDate, Set<Integer>> activeDayCalendars;
    private final List<LocalDate> days;
    private final LocalDate currentMonth;
    private final OnDayClickListener listener;
    private final Map<Integer, String> calendarIdToColor;
    private final Map<LocalDate, String> awardsMap;



    private static final int MAX_UNDERLINES = 3;

    public CalendarGridAdapter(List<LocalDate> days,
                               Map<LocalDate, Set<Integer>> activeDays,
                               LocalDate currentMonth,
                               Map<Integer, String> calendarIdToColor,
                               OnDayClickListener listener,
                               Map<LocalDate, String> awardsMap) {
        this.days = days;
        this.activeDayCalendars = activeDays;
        this.currentMonth = currentMonth;
        this.listener = listener;
        this.calendarIdToColor = calendarIdToColor;
        this.awardsMap = awardsMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocalDate date = days.get(position);

        holder.dayNumber.setText(String.valueOf(date.getDayOfMonth()));

        if (date.getMonthValue() != currentMonth.getMonthValue()) {
            holder.dayNumber.setTextColor(Color.parseColor("#AAAAAA"));
            holder.dayNumber.setTypeface(null, Typeface.NORMAL);
            holder.dayNumber.setBackground(null);
        } else if (date.equals(LocalDate.now())) {
            holder.dayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.royalblue));
            holder.dayNumber.setTypeface(null, Typeface.BOLD);
            holder.dayNumber.setBackgroundResource(R.drawable.today_circle_background);
        } else {
            holder.dayNumber.setTextColor(Color.BLACK);
            holder.dayNumber.setTypeface(null, Typeface.NORMAL);
            holder.dayNumber.setBackground(null);
        }

        holder.underlineContainer.removeAllViews();

        String award = awardsMap.get(date);
        if (award != null) {
            holder.awardIcon.setVisibility(View.VISIBLE);
            switch (award) {
                case "cup":
                    holder.awardIcon.setImageResource(R.drawable.ic_award_cup);
                    break;
                case "medal":
                    holder.awardIcon.setImageResource(R.drawable.ic_award_medal);
                    break;
                case "gold_border":
                    holder.awardIcon.setImageResource(R.drawable.ic_award_gold_border);
                    break;
                default:
                    holder.awardIcon.setVisibility(View.GONE);
            }
        } else {
            holder.awardIcon.setVisibility(View.GONE);
        }



        Set<Integer> calendars = activeDayCalendars.get(date);
        if (calendars != null) {
            int count = 0;
            for (Integer calId : calendars) {
                String hex = calendarIdToColor.getOrDefault(calId, "#67BA80");
                View underline = new View(holder.itemView.getContext());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(holder.itemView.getContext(), 64), dpToPx(holder.itemView.getContext(), 4));
                if (count > 0) params.setMargins(0, 12, 0, 0); // отступ сверху
                underline.setLayoutParams(params);
                underline.setBackgroundColor(Color.parseColor(hex));

                holder.underlineContainer.addView(underline);

                if (++count >= MAX_UNDERLINES) break;
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(date));
    }


    @Override
    public int getItemCount() {
        return days.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumber;
        LinearLayout underlineContainer;
        ImageView awardIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.dayNumber);
            underlineContainer = itemView.findViewById(R.id.underlineContainer);
            awardIcon = itemView.findViewById(R.id.awardIcon);
        }
    }

    private int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
