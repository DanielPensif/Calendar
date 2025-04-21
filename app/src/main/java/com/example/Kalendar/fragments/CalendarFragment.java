package com.example.Kalendar.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.CalendarManagerActivity;
import com.example.Kalendar.DayDetailsActivity;
import com.example.Kalendar.R;
import com.example.Kalendar.adapters.CalendarGridAdapter;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.utils.EventUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import java.util.*;

public class CalendarFragment extends Fragment {

    private AppDatabase db;

    private RecyclerView calendarGrid;
    private CalendarGridAdapter adapter;

    private int currentCalendarId = -1;
    private LocalDate currentDate;
    private List<LocalDate> daysInMonth = new ArrayList<>();
    Map<LocalDate, List<DayEntity>> dbDays = new HashMap<>();

    private final List<CalendarEntity> allCalendars = new ArrayList<>();

    private TextView streakText;
    private Spinner calendarSelector;

    private static final String[] QUOTES = {
            "Каждый день — это шанс начать заново.",
            "Успех — это сумма маленьких усилий, повторяемых изо дня в день.",
            "Сложности делают тебя сильнее.",
            "Сначала ты работаешь на результат, потом результат работает на тебя.",
            "Твоя цель — не быть лучше других, а быть лучше вчерашнего себя."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        TextView monthTitle = view.findViewById(R.id.monthTitle);
        monthTitle.setOnClickListener(v -> showMonthYearDialog());
        ImageButton prevBtn = view.findViewById(R.id.prevMonthBtn);
        ImageButton nextBtn = view.findViewById(R.id.nextMonthBtn);
        TextView quoteOfDay = view.findViewById(R.id.quoteOfDay);
        streakText = view.findViewById(R.id.streakText);
        calendarSelector = view.findViewById(R.id.calendarSelector);

        db = AppDatabase.getDatabase(requireContext());
        currentDate = LocalDate.now();

        // Установка цитаты
        String quote = QUOTES[new Random().nextInt(QUOTES.length)];
        quoteOfDay.setText(quote);

        // Название месяца
        updateMonthTitle(monthTitle);
        LinearLayout prevContainer = view.findViewById(R.id.prevMonthContainer);
        LinearLayout nextContainer = view.findViewById(R.id.nextMonthContainer);

        View.OnClickListener prevClick = v -> {
            currentDate = currentDate.minusMonths(1);
            updateCalendar();
            updateMonthTitle(monthTitle);
        };

        View.OnClickListener nextClick = v -> {
            currentDate = currentDate.plusMonths(1);
            updateCalendar();
            updateMonthTitle(monthTitle);
        };

        prevBtn.setOnClickListener(prevClick);
        nextBtn.setOnClickListener(nextClick);
        prevContainer.setOnClickListener(prevClick);
        nextContainer.setOnClickListener(nextClick);

        new Thread(() -> db.eventDao().deleteEventsWithMissingDay()).start();

        // Календарная сетка
        calendarGrid = view.findViewById(R.id.recycler_view);
        calendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));

        FloatingActionButton fab = view.findViewById(R.id.FloatingActionButton);
        fab.setOnClickListener(this::showFabMenu);

        loadCalendars();

        return view;
    }

    private void showMonthYearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Выбор месяца и года");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        NumberPicker monthPicker = dialogView.findViewById(R.id.monthPicker);
        NumberPicker yearPicker = dialogView.findViewById(R.id.yearPicker);

        String[] months = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(currentDate.getMonthValue());

        int thisYear = LocalDate.now().getYear();
        yearPicker.setMinValue(thisYear - 20);
        yearPicker.setMaxValue(thisYear + 20);
        yearPicker.setValue(currentDate.getYear());

        builder.setPositiveButton("Выбрать", (dialog, which) -> {
            int year = yearPicker.getValue();
            int month = monthPicker.getValue();
            currentDate = LocalDate.of(year, month, 1);
            updateCalendar();
            updateMonthTitle(requireView().findViewById(R.id.monthTitle));
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }



    private void updateMonthTitle(TextView view) {
        String[] nominativeMonths = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };

        int monthIndex = currentDate.getMonthValue() - 1;
        String name = nominativeMonths[monthIndex];
        view.setText(name + " " + currentDate.getYear());
    }
    private void generateCalendar(LocalDate date) {
        daysInMonth.clear();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon...7
        int shift = dayOfWeek - 1;
        LocalDate startDate = firstOfMonth.minusDays(shift);

        for (int i = 0; i < 42; i++) {
            daysInMonth.add(startDate.plusDays(i));
        }

        List<LocalDate> lastRow = daysInMonth.subList(35, 42);
        boolean allOutOfMonth = true;
        for (LocalDate d : lastRow) {
            if (d.getMonthValue() == date.getMonthValue()) {
                allOutOfMonth = false;
                break;
            }
        }
        if (allOutOfMonth) {
            daysInMonth = daysInMonth.subList(0, 35);
        }
    }


    private void updateCalendar() {
        generateCalendar(currentDate);
        loadDbDays();
    }

    private void loadDbDays() {
        new Thread(() -> {
            dbDays.clear();

            // Цвета календарей
            Map<Integer, String> colorMap = new HashMap<>();
            for (CalendarEntity c : allCalendars) {
                if (c != null) colorMap.put(c.id, c.colorHex);
            }

            // Загружаем дни по выбранному календарю
            List<DayEntity> days = (currentCalendarId == -1)
                    ? db.dayDao().getAll()
                    : db.dayDao().getByCalendarId(currentCalendarId);

            for (DayEntity day : days) {
                LocalDate date = Instant.ofEpochMilli(day.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                dbDays.computeIfAbsent(date, k -> new ArrayList<>()).add(day);
            }

            List<EventEntity> allEvents = db.eventDao().getAll();

            // Копия dbDays
            Map<LocalDate, List<DayEntity>> safeDbDays = new HashMap<>();
            for (Map.Entry<LocalDate, List<DayEntity>> entry : dbDays.entrySet()) {
                safeDbDays.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            // Активные дни (которые нужно подсветить)
            Map<LocalDate, Set<Integer>> activeDayCalendars = new HashMap<>();

            for (Map.Entry<LocalDate, List<DayEntity>> entry : safeDbDays.entrySet()) {
                LocalDate date = entry.getKey();
                List<DayEntity> dayList = entry.getValue();

                for (DayEntity day : dayList) {
                    boolean hasActiveTasks = !db.taskDao().getTasksForDay(day.id).isEmpty();

                    List<EventEntity> rawEvents = db.eventDao().getEventsForDay(day.id);
                    boolean hasActiveEvents = false;
                    for (EventEntity e : rawEvents) {
                        LocalDate d = Instant.ofEpochMilli(day.timestamp)
                                .atZone(ZoneId.systemDefault()).toLocalDate();

                        // Проверяем: если это НЕ повтор, то просто true
                        // если это повтор — он должен реально происходить в этот день
                        boolean occurs = e.repeatRule == null || e.repeatRule.isEmpty()
                                || EventUtils.occursOnDate(e, d, d); // start = date для инициализатора

                        if (occurs) {
                            hasActiveEvents = true;
                            break;
                        }
                    }

                    boolean active = hasActiveTasks || hasActiveEvents;

                    if (active) {
                        activeDayCalendars
                                .computeIfAbsent(date, k -> new HashSet<>())
                                .add(day.calendarId);
                    }
                }

                // Проверка повторов по существующим дням
                for (EventEntity e : allEvents) {
                    if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;
                    if (e.dayId == 0) continue;
                    DayEntity originalDay = db.dayDao().getById(e.dayId);
                    if (originalDay == null) continue;
                    if (currentCalendarId != -1 && e.calendarId != currentCalendarId) continue;

                    DayEntity original = db.dayDao().getById(e.dayId);
                    if (original == null) continue;

                    LocalDate start = Instant.ofEpochMilli(original.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    Log.d("CHECK", "Event: " + e.id + ", date: " + date + ", start: " + start + ", excluded: " + e.excludedDates);
                    boolean occurs = EventUtils.occursOnDate(e, date, start);
                    Log.d("CHECK", "Result = " + occurs);


                    if (EventUtils.occursOnDate(e, date, start)) {
                        activeDayCalendars
                                .computeIfAbsent(date, k -> new HashSet<>())
                                .add(e.calendarId);
                    }
                }
            }

            for (EventEntity e : allEvents) {
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;
                if (e.dayId == 0) continue;
                if (currentCalendarId != -1 && e.calendarId != currentCalendarId) continue;

                DayEntity original = db.dayDao().getById(e.dayId);
                if (original == null) continue;

                LocalDate start = Instant.ofEpochMilli(original.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                for (LocalDate date : daysInMonth) {
                    // 👇 исключаем реальные дни
                    if (dbDays.containsKey(date)) continue;

                    boolean occurs = EventUtils.occursOnDate(e, date, start);
                    if (occurs) {
                        Log.d("REPEAT_MATCH", "✔ " + date + " ← " + e.repeatRule);
                        activeDayCalendars
                                .computeIfAbsent(date, k -> new HashSet<>())
                                .add(e.calendarId);
                    }
                }
            }

            // Вывод в UI
            requireActivity().runOnUiThread(() -> {
                adapter = new CalendarGridAdapter(
                        daysInMonth,
                        activeDayCalendars,
                        currentDate,
                        colorMap,
                        this::onDayClick
                );
                calendarGrid.setAdapter(adapter);
            });
        }).start();
    }


    private void onDayClick(LocalDate date) {
        Intent intent = new Intent(requireContext(), DayDetailsActivity.class);
        intent.putExtra("date", date.toString());
        startActivity(intent);
    }

    private void loadCalendars() {
        int previousId = currentCalendarId;

        new Thread(() -> {
            List<CalendarEntity> calendars = db.calendarDao().getAll();

            List<String> titles = new ArrayList<>();
            List<CalendarEntity> loadedCalendars = new ArrayList<>();

            titles.add("Все календари");
            loadedCalendars.add(null); // индекс 0 — "все"

            for (CalendarEntity c : calendars) {
                titles.add(c.title);
                loadedCalendars.add(c);
            }

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                allCalendars.clear();
                allCalendars.addAll(loadedCalendars);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item, titles);
                calendarSelector.setAdapter(adapter);

                // восстановление выбранного календаря
                int newIndex = 0;
                for (int i = 0; i < allCalendars.size(); i++) {
                    CalendarEntity cal = allCalendars.get(i);
                    if (cal != null && cal.id == previousId) {
                        newIndex = i;
                        break;
                    }
                }
                calendarSelector.setSelection(newIndex);

                calendarSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        CalendarEntity selected = allCalendars.get(pos);
                        currentCalendarId = (selected != null) ? selected.id : -1;
                        updateCalendar();
                        updateStreak();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            });
        }).start();

    }


    private void updateStreak() {
        new Thread(() -> {
            List<DayEntity> days;
            if (currentCalendarId == -1) {
                days = db.dayDao().getAll();
            } else {
                days = db.dayDao().getByCalendarId(currentCalendarId);
            }

            int streak = 0;

            days.sort((d1, d2) -> Long.compare(d2.timestamp, d1.timestamp));

            for (DayEntity day : days) {
                List<TaskEntity> tasks = db.taskDao().getTasksForDay(day.id);
                List<EventEntity> events = db.eventDao().getEventsForDay(day.id);

                boolean hasTasks = !tasks.isEmpty();
                boolean allTasksDone = tasks.stream().allMatch(t -> t.done);

                boolean hasEvents = !events.isEmpty();
                boolean allEventsDone = events.stream().allMatch(e -> e.done);

                boolean allDone = (hasTasks && allTasksDone) || (hasEvents && allEventsDone);

                if (allDone) streak++;
                else break;
            }

            final int finalStreak = streak;
            requireActivity().runOnUiThread(() ->
                    streakText.setText("🔥 Стрик: " + finalStreak + " " + pluralize(finalStreak))
            );
        }).start();
    }

    private String pluralize(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;

        if (mod10 == 1 && mod100 != 11) return "день";
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return "дня";
        return "дней";
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(() -> db.eventDao().deleteEventsWithMissingDay()).start();
        loadCalendars();
        updateCalendar();
        updateStreak();
    }

    private void showFabMenu(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add("Добавить задачу/событие");
        menu.getMenu().add("Управление календарями");

        menu.setOnMenuItemClickListener(item -> {
            if (Objects.equals(item.getTitle(), "Добавить задачу/событие")) {
                showDayPickerDialog();
                } else if (Objects.equals(item.getTitle(), "Управление календарями")) {
                showCalendarManager();
                }
            return true;
        });

        menu.show();
    }

    private void showCalendarManager() {
        Intent intent = new Intent(requireContext(), CalendarManagerActivity.class);
        startActivity(intent);
    }

    private void showDayPickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext());
        dialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);

            Intent intent = new Intent(requireContext(), DayDetailsActivity.class);
            intent.putExtra("date", date.toString());
            intent.putExtra("calendarId", currentCalendarId);
            startActivity(intent);
        });
        dialog.show();
    }
    }
