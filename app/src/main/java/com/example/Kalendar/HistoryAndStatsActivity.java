package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.adapters.HistoryAdapter;
import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.utils.DatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryAndStatsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private RecyclerView historyRecyclerView;
    private ImageView flashEffect;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;
    private Spinner spinnerDaysRange;
    private TextView textGraphTitle;
    private int selectedDays = 7;

    // однопоточный исполнитель для всех БД-задач
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_stats);

        lineChart = findViewById(R.id.lineChart);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        flashEffect = findViewById(R.id.flashEffect);
        textGraphTitle = findViewById(R.id.textGraphTitle);

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerDaysRange = findViewById(R.id.spinnerDaysRange);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this,
                R.array.days_range_options,
                android.R.layout.simple_spinner_item
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDaysRange.setAdapter(adapterSpinner);
        spinnerDaysRange.setSelection(0);

        spinnerDaysRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] dayOptions = {7, 14, 30, 60, 90};
                selectedDays = dayOptions[position];
                loadGraphDataAsync();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner spinnerSortOptions = findViewById(R.id.spinnerSortOptions);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOptions.setAdapter(sortAdapter);
        spinnerSortOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortHistory(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadGraphDataAsync();
        loadHistoryAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGraphDataAsync();
    }

    private void loadGraphDataAsync() {
        dbExecutor.execute(() -> {
            List<int[]> stats = DatabaseHelper.getDetailedTaskStatsForLastNDays(this, selectedDays);
            runOnUiThread(() -> updateChart(stats));
        });
    }

    private void updateChart(List<int[]> stats) {
        List<Entry> totalEntries = new ArrayList<>();
        List<Entry> completedEntries = new ArrayList<>();
        List<Entry> notCompletedEntries = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            int[] day = stats.get(i);
            totalEntries.add(new Entry(i, day[0]));
            completedEntries.add(new Entry(i, day[1]));
            notCompletedEntries.add(new Entry(i, day[2]));
        }

        LineDataSet totalDataSet = new LineDataSet(totalEntries, "Все задачи");
        totalDataSet.setColor(0xFF1E88E5);
        totalDataSet.setCircleColor(0xFF1E88E5);

        LineDataSet completedDataSet = new LineDataSet(completedEntries, "Выполненные");
        completedDataSet.setColor(0xFFFFD700);
        completedDataSet.setCircleColor(0xFFFFD700);

        LineDataSet notCompletedDataSet = new LineDataSet(notCompletedEntries, "Невыполненные");
        notCompletedDataSet.setColor(0xFFFF4444);
        notCompletedDataSet.setCircleColor(0xFFFF4444);

        for (LineDataSet set : List.of(totalDataSet, completedDataSet, notCompletedDataSet)) {
            set.setLineWidth(2f);
            set.setCircleRadius(4f);
            set.setValueTextSize(14f); // увеличенный текст
        }

        LineData lineData = new LineData(totalDataSet, completedDataSet, notCompletedDataSet);
        lineData.setDrawValues(false);

        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f); // увеличенный текст
        xAxis.setLabelRotationAngle(-45);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
            private final Calendar calendar = Calendar.getInstance();
            @Override
            public String getFormattedValue(float value) {
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.DAY_OF_YEAR, (int) value - (selectedDays - 1));
                return dateFormat.format(calendar.getTime());
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(14f); // увеличенный текст
        leftAxis.setAxisMinimum(0f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.getLegend().setTextSize(14f); // увеличенный текст

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void loadHistoryAsync() {
        dbExecutor.execute(() -> {
            List<HistoryItem> items = DatabaseHelper.getCompletedDays(this);
            Map<Pair<Long, Integer>, String> awardsMap = DatabaseHelper.getAwardsForCompletedDays(this);
            for (HistoryItem item : items) {
                Pair<Long, Integer> key = new Pair<>(item.timestamp, item.calendarId);
                if (awardsMap.containsKey(key)) {
                    item.award = awardsMap.get(key);
                }
            }
            runOnUiThread(() -> {
                historyItems = items;
                adapter = new HistoryAdapter(historyItems, this::showAwardDialog);
                historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                historyRecyclerView.setAdapter(adapter);
                sortHistory(0);
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortHistory(int sortMode) {
        if (historyItems == null) return;
        switch (sortMode) {
            case 0:
                historyItems.sort((a, b) -> {
                    int cmp = Long.compare(b.timestamp, a.timestamp);
                    return cmp != 0 ? cmp : a.calendarName.compareToIgnoreCase(b.calendarName);
                });
                break;
            case 1:
                historyItems.sort((a, b) -> {
                    int cmp = Long.compare(a.timestamp, b.timestamp);
                    return cmp != 0 ? cmp : a.calendarName.compareToIgnoreCase(b.calendarName);
                });
                break;
            case 2:
                historyItems.sort((a, b) -> {
                    int cmp = a.calendarName.compareToIgnoreCase(b.calendarName);
                    return cmp != 0 ? cmp : Long.compare(b.timestamp, a.timestamp);
                });
                break;
            case 3:
                historyItems.sort((a, b) -> {
                    int cmp = b.calendarName.compareToIgnoreCase(a.calendarName);
                    return cmp != 0 ? cmp : Long.compare(b.timestamp, a.timestamp);
                });
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void showAwardDialog(int position) {
        String[] awards = {"🏆 Кубок", "🎖️ Медаль", "🔲 Рамка"};
        HistoryItem item = historyItems.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Выбери награду")
                .setItems(awards, (dialog, which) -> {
                    flashEffect.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_success));
                    dbExecutor.execute(() -> {
                        int calendarId = DatabaseHelper.getDatabase(this)
                                .calendarDao().getIdByName(item.calendarName);
                        int dayId = DatabaseHelper.getDayIdByTimestampAndCalendarId(
                                this, item.timestamp, calendarId
                        );
                        if (dayId != -1) {
                            String awardCode = switch (which) {
                                case 0 -> "cup";
                                case 1 -> "medal";
                                case 2 -> "gold_border";
                                default -> null;
                            };
                            if (awardCode != null) {
                                DatabaseHelper.saveAwardForDay(this, dayId, awardCode);
                                runOnUiThread(() -> adapter.notifyItemChanged(position));
                            }
                        }
                    });
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}