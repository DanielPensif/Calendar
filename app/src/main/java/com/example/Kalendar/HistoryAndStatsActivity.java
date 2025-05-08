package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryAndStatsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private RecyclerView historyRecyclerView;
    private ImageView flashEffect;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;
    private Spinner spinnerDaysRange;
    private TextView textGraphTitle;
    private int selectedDays = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_stats);

        lineChart = findViewById(R.id.lineChart);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        flashEffect = findViewById(R.id.flashEffect);
        textGraphTitle = findViewById(R.id.textGraphTitle);

        // Добавляем Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerDaysRange = findViewById(R.id.spinnerDaysRange);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.days_range_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDaysRange.setAdapter(adapter);
        spinnerDaysRange.setSelection(0); // 7 дней по умолчанию

        spinnerDaysRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int[] dayOptions = {7, 30, 90, 180, 365};
                selectedDays = dayOptions[position];
                loadGraphData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadGraphData();
        loadHistory();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadGraphData(); // Перезагружаем график при возвращении на экран
    }

    private void loadGraphData() {
        List<Integer> totalTasks = DatabaseHelper.getTaskCountsForLastNDays(this, selectedDays, false);
        List<Integer> completedTasks = DatabaseHelper.getTaskCountsForLastNDays(this, selectedDays, true);

        List<Entry> totalEntries = new ArrayList<>();
        List<Entry> completedEntries = new ArrayList<>();

        for (int i = 0; i < totalTasks.size(); i++) {
            totalEntries.add(new Entry(i, totalTasks.get(i)));
            completedEntries.add(new Entry(i, completedTasks.get(i)));
        }

        LineDataSet totalDataSet = new LineDataSet(totalEntries, "Все задачи");
        totalDataSet.setColor(0xFF1E88E5);
        totalDataSet.setCircleColor(0xFF1E88E5);
        totalDataSet.setLineWidth(2f);
        totalDataSet.setCircleRadius(4f);

        LineDataSet completedDataSet = new LineDataSet(completedEntries, "Выполненные задачи");
        completedDataSet.setColor(0xFFFFD700);
        completedDataSet.setCircleColor(0xFFFFD700);
        completedDataSet.setLineWidth(2f);
        completedDataSet.setCircleRadius(4f);

        LineData lineData = new LineData(totalDataSet, completedDataSet);
        lineData.setDrawValues(false);

        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
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
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);

        lineChart.animateX(1000);
        lineChart.invalidate();
    }



    private void loadHistory() {
        historyItems = DatabaseHelper.getCompletedDays(this);
        adapter = new HistoryAdapter(historyItems, this::showAwardDialog);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
    }

    private void showAwardDialog(int position) {
        String[] awards = {"🏆 Кубок", "🎖️ Медаль", "🔲 Рамка"};

        new AlertDialog.Builder(this)
                .setTitle("Выбери награду")
                .setItems(awards, (dialog, which) -> {
                    flashEffect.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_success));
                })
                .show();
    }
}
