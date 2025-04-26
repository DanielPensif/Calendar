package com.example.Kalendar;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.Kalendar.adapters.HistoryAdapter;
import com.example.Kalendar.models.CompletedDay;
import java.util.ArrayList;
import java.util.List;

public class HistoryAndStatsActivity extends AppCompatActivity {

    private CustomGraphView customGraphView;
    private RecyclerView historyRecyclerView;
    private ImageView flashEffect;
    private HistoryAdapter adapter;
    private List<CompletedDay> completedDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_stats);

        customGraphView = findViewById(R.id.customGraphView);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        flashEffect = findViewById(R.id.flashEffect);

        loadChartData();
        loadHistory();
    }

    private void loadChartData() {
        List<Integer> taskCounts = new ArrayList<>();
        taskCounts.add(3);
        taskCounts.add(5);
        taskCounts.add(2);
        taskCounts.add(4);
        taskCounts.add(6);
        taskCounts.add(5);
        taskCounts.add(7);

        customGraphView.setTaskData(taskCounts);
    }

    private void loadHistory() {
        completedDays.add(new CompletedDay("1 мая"));
        completedDays.add(new CompletedDay("3 мая"));
        completedDays.add(new CompletedDay("5 мая"));

        adapter = new HistoryAdapter(completedDays, this::showAwardDialog);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
    }

    private void showAwardDialog(int position) {
        String[] awards = {"🏆 Кубок", "🎖️ Медаль", "🔲 Рамка"};

        new AlertDialog.Builder(this)
                .setTitle("Выбери награду")
                .setItems(awards, (dialog, which) -> {
                    completedDays.get(position).setAwardType(which);
                    adapter.notifyItemChanged(position);
                    flashEffect.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_success));
                })
                .show();
    }
}
