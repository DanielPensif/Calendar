package com.example.Kalendar.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.os.Handler;
import android.view.*;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.HomeAdapter;
import com.example.Kalendar.adapters.HomeItem;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.utils.EventUtils;

import org.json.*;
import org.json.JSONObject;
import org.threeten.bp.*;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.*;
import java.util.function.*;

import okhttp3.*;

public class HomeFragment extends Fragment {
    private ProgressBar quoteProgress;
    private Button btnNewQuote;
    private TextView textTime, textDayMonth, textEmpty, textAllDone, textQuote;
    private HomeAdapter homeAdapter;
    private final List<HomeItem> homeItems = new ArrayList<>();
    private final List<TaskEntity> tasks = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable timeUpdater;



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

        quoteProgress = view.findViewById(R.id.quoteProgress);
        btnNewQuote = view.findViewById(R.id.btnNewQuote);

        textQuote = view.findViewById(R.id.textQuote);
        getQuoteForToday(quote -> {textQuote.setText(quote);});

        Button btnNewQuote = view.findViewById(R.id.btnNewQuote);

        btnNewQuote.setOnClickListener(v -> {
            setQuoteLoading(true);
            getQuoteForToday(quote -> {
                requireActivity().runOnUiThread(() -> {
                    textQuote.setText(quote);
                    setQuoteLoading(false);
                });
            });
        });

        timeUpdater = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    updateDateTime();
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timeUpdater);

        textEmpty = view.findViewById(R.id.textEmpty);
        textAllDone = view.findViewById(R.id.textAllDone);

        loadTodayContent();

        return view;
    }

    private void loadTodayContent() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            int userId = SessionManager.getLoggedInUserId(requireContext());

            // Получаем календари пользователя
            List<Integer> calendarIds = new ArrayList<>();
            db.calendarDao().getAllForUser(userId).forEach(c -> calendarIds.add(c.id));

            long timestamp = LocalDate.now()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant().toEpochMilli();

            // Получаем DayEntity для этих календарей
            List<DayEntity> todayDays = db.dayDao().getByTimestampAndCalendarIds(timestamp, calendarIds);

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
            // Повторы по другим событиям
            List<EventEntity> allEvents = db.eventDao().getAll();
            LocalDate today = LocalDate.now();
            Set<Integer> accountedIds = new HashSet<>();
            for (EventEntity e : todayEvents) accountedIds.add(e.id);

            for (EventEntity e : allEvents) {
                if (accountedIds.contains(e.id)) continue;
                if (e.repeatRule == null || e.repeatRule.isEmpty()) continue;
                if (!calendarIds.contains(e.calendarId)) continue; // фильтрация по пользователю

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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timeUpdater);
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
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

        textDayMonth.setText(formattedDate);
        textTime.setText(time.format(timeFormatter));
    }


    private void setQuoteLoading(boolean isLoading) {
        quoteProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isLoading) {
            textQuote.setVisibility(View.GONE);
            btnNewQuote.setEnabled(false);
        } else {
            textQuote.setAlpha(0f);
            textQuote.setVisibility(View.VISIBLE);
            textQuote.animate().alpha(1f).setDuration(500).start();
            btnNewQuote.setEnabled(true);
        }
    }

    private void getQuoteForToday(Consumer<String> callback) {
        OkHttpClient client = new OkHttpClient();
        String currentQuote = textQuote.getText().toString();

        final int maxAttempts = 10;

        fetchUniqueQuote(client, currentQuote, callback, 0, maxAttempts);
    }

    private void fetchUniqueQuote(OkHttpClient client, String currentQuote, Consumer<String> callback, int attempt, int maxAttempts) {
        Request request = new Request.Builder()
                .url("http://api.forismatic.com/api/1.0/?method=getQuote&format=json&lang=ru")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String quote = jsonObject.getString("quoteText").trim();

                        if (quote.equals(currentQuote) && attempt < maxAttempts) {
                            // Повторная попытка
                            fetchUniqueQuote(client, currentQuote, callback, attempt + 1, maxAttempts);
                        } else {
                            requireActivity().runOnUiThread(() -> callback.accept(quote));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        fallback();
                    }
                } else {
                    fallback();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                fallback();
            }

            private void fallback() {
                requireActivity().runOnUiThread(() ->
                        callback.accept("Не удалось загрузить новую цитату. Попробуйте позже."));
            }
        });
    }

}