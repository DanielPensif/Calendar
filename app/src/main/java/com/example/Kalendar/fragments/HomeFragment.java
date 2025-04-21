package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.HomeAdapter;
import com.example.Kalendar.adapters.HomeItem;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.utils.EventUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.*;

public class HomeFragment extends Fragment {

    private TextView textTime, textDayMonth, textEmpty, textAllDone, textQuote;

    private String lastQuote = "";

    private HomeAdapter homeAdapter;
    private final List<HomeItem> homeItems = new ArrayList<>();

    private final List<TaskEntity> tasks = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        homeAdapter = new HomeAdapter(homeItems, requireContext(), this::loadTodayContent);
        tasksRecyclerView.setAdapter(homeAdapter);

        loadTodayContent();


        textTime = view.findViewById(R.id.textTime);
        textDayMonth = view.findViewById(R.id.textDayMonth);

        textQuote = view.findViewById(R.id.textQuote);
        textQuote.setText(getQuoteForToday());

        Button btnNewQuote = view.findViewById(R.id.btnNewQuote);
        textQuote.setText(getQuoteForToday());

        btnNewQuote.setOnClickListener(v -> {
            String newQuote = getRandomQuoteDifferentFromLast();
            textQuote.setText(newQuote);
            lastQuote = newQuote;
        });

        updateDateTime();

        textEmpty = view.findViewById(R.id.textEmpty);
        textAllDone = view.findViewById(R.id.textAllDone);

        loadTodayContent();

        return view;
    }

    private void loadTodayContent() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());

            long timestamp = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            List<DayEntity> todayDays = db.dayDao().getByTimestamp(timestamp);

            List<TaskEntity> todayTasks = new ArrayList<>();
            List<EventEntity> todayEvents = new ArrayList<>();

            for (DayEntity day : todayDays) {
                todayTasks.addAll(db.taskDao().getTasksForDay(day.id));

                for (EventEntity e : db.eventDao().getEventsForDay(day.id)) {
                    LocalDate start = Instant.ofEpochMilli(day.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate();

                    if (e.repeatRule != null && !e.repeatRule.isEmpty()) {
                        if (EventUtils.occursOnDate(e, LocalDate.now(), start)) {
                            todayEvents.add(createVirtualCopy(e, day));
                        }
                    } else {
                        todayEvents.add(e);
                    }
                }
            }

            List<EventEntity> allEvents = db.eventDao().getAll();
            LocalDate today = LocalDate.now();
            Set<Integer> accountedIds = new HashSet<>();
            for (EventEntity e : todayEvents) accountedIds.add(e.id);

            for (EventEntity e : allEvents) {
                if (accountedIds.contains(e.id)) continue;
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;

                DayEntity base = db.dayDao().getById(e.dayId);
                if (base == null) continue;

                LocalDate start = Instant.ofEpochMilli(base.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                if (EventUtils.occursOnDate(e, today, start)) {
                    todayEvents.add(createVirtualCopy(e, base));
                }
            }


            requireActivity().runOnUiThread(() -> {
                renderHome(todayTasks, todayEvents);
            });
        }).start();
    }

    private EventEntity createVirtualCopy(EventEntity original, DayEntity day) {
        EventEntity copy = new EventEntity();

        copy.id = 0; // ID = 0 означает, что это виртуальное, не сохранённое в БД
        copy.title = original.title;
        copy.timeStart = original.timeStart;
        copy.timeEnd = original.timeEnd;
        copy.allDay = original.allDay;
        copy.category = original.category;
        copy.description = original.description;
        copy.location = original.location;
        copy.repeatRule = original.repeatRule;
        copy.calendarId = original.calendarId;
        copy.dayId = day.id;
        copy.excludedDates = original.excludedDates;

        // Устанавливаем дату (для сравнения и вывода)
        LocalDate localDate = Instant.ofEpochMilli(day.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        copy.date = localDate.toString();

        return copy;
    }


    @Override
    public void onResume() {
        super.onResume();
        loadTodayContent();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void renderHome(List<TaskEntity> tasks, List<EventEntity> events) {
        homeItems.clear();

        if (!events.isEmpty()) {
            homeItems.add(new HomeItem.Header("События"));
            for (EventEntity e : events) {
                homeItems.add(new HomeItem.EventItem(e));
            }
        }

        if (!tasks.isEmpty()) {
            homeItems.add(new HomeItem.Header("Задачи"));
            for (TaskEntity t : tasks) {
                homeItems.add(new HomeItem.TaskItem(t));
            }
        }

        textEmpty.setVisibility(tasks.isEmpty() && events.isEmpty() ? View.VISIBLE : View.GONE);
        textAllDone.setVisibility(tasks.stream().allMatch(t -> t.done) && !tasks.isEmpty() ? View.VISIBLE : View.GONE);

        homeAdapter.notifyDataSetChanged();
    }


    private void updateDateTime() {
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.now();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", new Locale("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = today.format(dateFormatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1); // заглавная первая буква

        textDayMonth.setText(formattedDate);
        textTime.setText(time.format(timeFormatter));
    }

    private String getQuoteForToday() {
        List<String> quotes = Arrays.asList(
                "Ты можешь больше, чем думаешь.",
                "Каждое великое достижение начинается с решения попробовать.",
                "Прогресс — лучше, чем совершенство.",
                "Делай сегодня то, что другие не хотят — завтра будешь жить так, как другие не могут.",
                "Секрет продвижения вперёд в том, чтобы начать.",
                "Не откладывай мечты. Сделай первый шаг."
        );

        int dayOfYear = LocalDate.now().getDayOfYear();
        return quotes.get(dayOfYear % quotes.size());
    }

    private String getRandomQuoteDifferentFromLast() {
        List<String> quotes = Arrays.asList(
                "Ты можешь больше, чем думаешь.",
                "Каждое великое достижение начинается с решения попробовать.",
                "Прогресс — лучше, чем совершенство.",
                "Делай сегодня то, что другие не хотят — завтра будешь жить так, как другие не могут.",
                "Секрет продвижения вперёд в том, чтобы начать.",
                "Не откладывай мечты. Сделай первый шаг."
        );

        Random rand = new Random();
        String quote;
        do {
            quote = quotes.get(rand.nextInt(quotes.size()));
        } while (quote.equals(lastQuote));

        return quote;
    }


}