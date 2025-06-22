package com.example.Kalendar;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.adapters.HistoryAdapter;
import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.viewmodel.HistoryAndStatsViewModel;
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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryAndStatsActivity extends AppCompatActivity {
    private LineChart lineChart;
    private RecyclerView historyRv;
    private Spinner daysSpinner, sortSpinner;
    private TextView cdiText;

    private HistoryAndStatsViewModel vm;
    private HistoryAdapter historyAdapter;
    private List<int[]> detailStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_stats);

        // UI
        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        lineChart = findViewById(R.id.lineChart);
        historyRv = findViewById(R.id.historyRecyclerView);
        daysSpinner = findViewById(R.id.spinnerDaysRange);
        sortSpinner = findViewById(R.id.spinnerSortOptions);
        cdiText = findViewById(R.id.cdiText);

        // ViewModel
        vm = new ViewModelProvider(this).get(HistoryAndStatsViewModel.class);

        setupDaysSpinner();
        setupSortSpinner();
        setupCdiDialog();

        // Подписка на график
        vm.stats.observe(this, bundle -> {
            updateChart(bundle.total, bundle.done, bundle.detail);
            detailStats = bundle.detail;
        });

        // Подписка на историю
        vm.history.observe(this, items -> {
            historyAdapter = new HistoryAdapter(items, this::showAwardDialog);
            historyRv.setLayoutManager(new LinearLayoutManager(this));
            historyRv.setAdapter(historyAdapter);
            sortHistory(sortSpinner.getSelectedItemPosition());
        });
        vm.reloadHistory();
    }

    private void setupDaysSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.days_range_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daysSpinner.setAdapter(adapter);
        daysSpinner.setSelection(0);
        daysSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            final int[] opts = {7, 14, 30, 60, 90};
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                vm.setDays(opts[pos]);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setSelection(0);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                sortHistory(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void setupCdiDialog() {
        cdiText.setOnClickListener(v -> {
            SpannableString ss = new SpannableString("История выполненных дней✔");
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1E88E5")),
                    ss.length()-1, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            new AlertDialog.Builder(this)
                    .setTitle(ss)
                    .setMessage("Здесь отображается вся история ваших выполненных дней.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void updateChart(List<Integer> totalTasks,
                             List<Integer> completedTasks,
                             List<int[]> stats) {
        List<Entry> totalEntries = new ArrayList<>();
        List<Entry> doneEntries  = new ArrayList<>();
        List<Entry> notDoneEntries = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            int[] day = stats.get(i);
            totalEntries.add(new Entry(i, day[0]));
            doneEntries.add(new Entry(i, day[1]));
            notDoneEntries.add(new Entry(i, day[2]));
        }

        LineDataSet dsTotal = new LineDataSet(totalEntries, "Все задачи");
        LineDataSet dsDone  = new LineDataSet(doneEntries,  "Выполненные");
        LineDataSet dsNot   = new LineDataSet(notDoneEntries, "Невыполненные");

        for (LineDataSet ds : List.of(dsTotal, dsDone, dsNot)) {
            ds.setLineWidth(2f);
            ds.setCircleRadius(4f);
            ds.setValueTextSize(12f);
        }
        dsTotal.setColor(0xFF1E88E5);    dsTotal.setCircleColor(0xFF1E88E5);
        dsDone.setColor(0xFFFFD700);     dsDone.setCircleColor(0xFFFFD700);
        dsNot.setColor(0xFFFF4444);      dsNot.setCircleColor(0xFFFF4444);

        LineData data = new LineData(dsTotal, dsDone, dsNot);
        data.setDrawValues(false);
        lineChart.setData(data);

        // XAxis
        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat df = new SimpleDateFormat("d MMM", Locale.getDefault());
            private final Calendar cal = Calendar.getInstance();
            @Override public String getFormattedValue(float val) {
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.DAY_OF_YEAR, (int)val - (daysSpinner.getSelectedItemPosition()==0?6: Integer.parseInt(daysSpinner.getSelectedItem().toString())-1));
                return df.format(cal.getTime());
            }
        });

        // YAxis
        YAxis left = lineChart.getAxisLeft();
        left.setGranularity(1f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setTextSize(12f);

        lineChart.animateX(800);
        lineChart.invalidate();

        detailStats = stats;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sortHistory(int mode) {
        if (historyAdapter == null) return;
        List<HistoryItem> items = (List<HistoryItem>) historyAdapter.getItems();
        switch (mode) {
            case 0: // по дате DESC
                items.sort((a,b)-> Long.compare(b.timestamp,a.timestamp));
                break;
            case 1: // по дате ASC
                items.sort((a,b)-> Long.compare(a.timestamp,b.timestamp));
                break;
            case 2: // по имени ASC
                items.sort((a,b)-> a.calendarName.compareToIgnoreCase(b.calendarName));
                break;
            case 3: // по имени DESC
                items.sort((a,b)-> b.calendarName.compareToIgnoreCase(a.calendarName));
                break;
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void showAwardDialog(int position) {
        HistoryItem item = historyAdapter.getItems().get(position);
        String[] awards = {"🏆 Кубок", "🎖️ Медаль", "🔲 Рамка"};
        new AlertDialog.Builder(this)
                .setTitle("Выбери награду")
                .setItems(awards, (dialog, which) -> {
                    int uid = SessionManager.getLoggedInUserId(this);
                    vm.saveAward(item.timestamp, item.calendarName, which, uid);
                }).show();
    }
}
