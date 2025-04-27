package com.example.Kalendar;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Kalendar.adapters.HistoryAdapter;
import com.example.Kalendar.CustomGraphView;
import com.example.Kalendar.adapters.HistoryItem;
import com.example.Kalendar.utils.DatabaseHelper;

import java.util.List;

public class HistoryAndStatsActivity extends AppCompatActivity {

    private CustomGraphView customGraphView;
    private RecyclerView historyRecyclerView;
    private ImageView flashEffect;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_stats);

        customGraphView = findViewById(R.id.customGraphView);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        flashEffect = findViewById(R.id.flashEffect);

        loadGraphData();
        loadHistory();
    }

    private void loadGraphData() {
        List<Integer> totalTasks = DatabaseHelper.getTaskCountsForLast7Days(this, false);
        List<Integer> completedTasks = DatabaseHelper.getTaskCountsForLast7Days(this, true);
        customGraphView.setData(totalTasks, completedTasks);
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
