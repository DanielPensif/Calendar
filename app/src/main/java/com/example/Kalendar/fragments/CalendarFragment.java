package com.example.Kalendar.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.CalendarManagerActivity;
import com.example.Kalendar.DayDetailsActivity;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.CalendarGridAdapter;
import com.example.Kalendar.adapters.CalendarSpinnerAdapter;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.viewmodel.CalendarContent;
import com.example.Kalendar.viewmodel.CalendarViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDate;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {
    private CalendarViewModel viewModel;
    private RecyclerView calendarGrid;
    private Spinner calendarSelector;
    private TextView monthTitle, streakText, quoteOfDay;
    private FloatingActionButton fab;
    private LocalDate currentDate = LocalDate.now();
    private int currentCalendarId = -1;

    private static final String[] months = {
            "Январь","Февраль","Март","Апрель","Май","Июнь",
            "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
    };
    private static final String[] QUOTES = {
            "Каждый день — это шанс начать заново.",
            "Успех — это сумма маленьких усилий, повторяемых изо дня в день.",
            "Сложности делают тебя сильнее.",
            "Сначала ты работаешь на результат, потом результат работает на тебя.",
            "Твоя цель — не быть лучше других, а быть лучше вчерашнего себя."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle saved) {
        View v = inf.inflate(R.layout.fragment_calendar, container, false);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        monthTitle       = v.findViewById(R.id.monthTitle);
        calendarGrid     = v.findViewById(R.id.recycler_view);
        calendarSelector = v.findViewById(R.id.calendarSelector);
        streakText       = v.findViewById(R.id.streakText);
        quoteOfDay       = v.findViewById(R.id.quoteOfDay);
        fab              = v.findViewById(R.id.FloatingActionButton);

        monthTitle.setOnClickListener(x -> showMonthYearDialog());
        v.findViewById(R.id.prevMonthBtn).setOnClickListener(x -> {
            currentDate = currentDate.minusMonths(1);
            updateMonthTitle();
            reload();
        });
        v.findViewById(R.id.nextMonthBtn).setOnClickListener(x -> {
            currentDate = currentDate.plusMonths(1);
            updateMonthTitle();
            reload();
        });

        quoteOfDay.setText(QUOTES[new java.util.Random().nextInt(QUOTES.length)]);
        streakText.setOnClickListener(x -> new AlertDialog.Builder(requireContext())
                .setView(R.layout.dialog_streak_info)
                .create().show());

        calendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                CalendarEntity sel = (CalendarEntity) parent.getItemAtPosition(pos);
                currentCalendarId = (sel != null ? sel.getId() : -1);
                reload();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        fab.setOnClickListener(a -> showFabMenu(a));
        updateMonthTitle();
        reload();
        return v;
    }

    private void reload() {
        int userId = SessionManager.getLoggedInUserId(requireContext());
        viewModel.loadCalendar(userId, currentCalendarId, currentDate)
                .observe(getViewLifecycleOwner(), this::render);
    }

    @SuppressWarnings("NotifyDataSetChanged")
    private void render(CalendarContent cc) {
        // Устанавливаем адаптер со списком календарей
        calendarSelector.setAdapter(
                new CalendarSpinnerAdapter(requireContext(), cc.allCalendars)
        );
        // Восстанавливаем выбранный элемент
        List<CalendarEntity> list = cc.allCalendars;
        int selIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            CalendarEntity cal = list.get(i);
            if (cal != null && cal.getId() == cc.currentCalendarId) {
                selIndex = i;
                break;
            }
        }
        calendarSelector.setSelection(selIndex, false);

        updateMonthTitle();
        calendarGrid.setAdapter(new CalendarGridAdapter(
                cc.daysInMonth,
                cc.activeDayCalendars,
                currentDate,
                cc.colorMap,
                this::onDayClick,
                cc.awardsMap,
                cc.currentCalendarId
        ));
        streakText.setText("🔥 Стрик: " + cc.streak + " " + pluralize(cc.streak));
    }

    private void updateMonthTitle() {
        monthTitle.setText(months[currentDate.getMonthValue()-1] + " " + currentDate.getYear());
    }

    private void showMonthYearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Выбор месяца и года");
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        NumberPicker monthPicker = dialogView.findViewById(R.id.monthPicker);
        NumberPicker yearPicker  = dialogView.findViewById(R.id.yearPicker);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(currentDate.getMonthValue());
        int thisYear = LocalDate.now().getYear();
        yearPicker.setMinValue(thisYear - 20);
        yearPicker.setMaxValue(thisYear + 20);
        yearPicker.setValue(currentDate.getYear());

        builder.setPositiveButton("Выбрать", (dialog, which) -> {
            int year  = yearPicker.getValue();
            int month = monthPicker.getValue();
            currentDate = LocalDate.of(year, month, 1);
            updateMonthTitle();
            reload();
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showFabMenu(View anchor) {
        PopupMenu m = new PopupMenu(requireContext(), anchor);
        m.getMenu().add("Добавить задачу/событие");
        m.getMenu().add("Управление календарями");
        m.setOnMenuItemClickListener(it -> {
            if (it.getTitle().equals("Добавить задачу/событие"))
                showDayPickerDialog();
            else
                startActivity(new Intent(requireContext(), CalendarManagerActivity.class));
            return true;
        });
        m.show();
    }

    private void showDayPickerDialog() {
        new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
            Intent i = new Intent(requireContext(), DayDetailsActivity.class);
            i.putExtra("date", LocalDate.of(y, m+1, d).toString());
            i.putExtra("calendarId", currentCalendarId);
            startActivity(i);
        },
                currentDate.getYear(),
                currentDate.getMonthValue()-1,
                currentDate.getDayOfMonth())
                .show();
    }

    private void onDayClick(LocalDate date) {
        Intent i = new Intent(requireContext(), DayDetailsActivity.class);
        i.putExtra("date", date.toString());
        i.putExtra("calendarId", currentCalendarId);
        startActivity(i);
    }

    private String pluralize(int c) {
        int m10=c%10, m100=c%100;
        if (m10==1 && m100!=11) return "день";
        if (m10>=2 && m10<=4 && (m100<10||m100>=20)) return "дня";
        return "дней";
    }
}