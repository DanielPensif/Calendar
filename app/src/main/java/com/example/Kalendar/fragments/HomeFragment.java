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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.example.Kalendar.R;
import com.example.Kalendar.adapters.HomeAdapter;
import com.example.Kalendar.adapters.HomeItem;
import com.example.Kalendar.adapters.SessionManager;
import com.example.Kalendar.viewmodel.HomeContent;
import com.example.Kalendar.viewmodel.HomeViewModel;

import org.threeten.bp.*;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private ProgressBar quoteProgress;
    private Button btnNewQuote;
    private TextView textTime, textDayMonth, textEmpty, textAllDone, textQuote;
    private HomeAdapter homeAdapter;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.tasksRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        homeAdapter = new HomeAdapter(
                new ArrayList<>(),
                requireContext(),
                () -> viewModel.loadToday(SessionManager.getLoggedInUserId(requireContext()))
                        .observe(getViewLifecycleOwner(), this::renderHome)
        );
        rv.setAdapter(homeAdapter);

        // UI элементы
        textTime  = view.findViewById(R.id.textTime);
        textDayMonth = view.findViewById(R.id.textDayMonth);
        quoteProgress = view.findViewById(R.id.quoteProgress);
        btnNewQuote   = view.findViewById(R.id.btnNewQuote);
        textQuote     = view.findViewById(R.id.textQuote);
        textEmpty     = view.findViewById(R.id.textEmpty);
        textAllDone   = view.findViewById(R.id.textAllDone);

        // Запуск таймера отображения времени
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

        btnNewQuote.setOnClickListener(v -> {
            setQuoteLoading(true);
            // Цитата фетчится прямо в VM
            viewModel.getQuote().observe(getViewLifecycleOwner(), quote -> {
                textQuote.setText(quote);
                setQuoteLoading(false);
            });
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Подписываемся на контент «Домашней» страницы
        viewModel.loadToday(SessionManager.getLoggedInUserId(requireContext()))
                .observe(getViewLifecycleOwner(), this::renderHome);

        // Подписываемся сразу на первую цитату
        setQuoteLoading(true);
        viewModel.getQuote().observe(getViewLifecycleOwner(), quote -> {
            textQuote.setText(quote);
            setQuoteLoading(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // при каждом возвращении обновляем домашний контент
        viewModel.loadToday(SessionManager.getLoggedInUserId(requireContext()))
                .observe(getViewLifecycleOwner(), this::renderHome);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(timeUpdater);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void renderHome(HomeContent hc) {
        homeAdapter.setItems(new java.util.ArrayList<HomeItem>() {{
            if (!hc.events.isEmpty()) {
                add(new HomeItem.Header("События"));
                for (var e : hc.events) add(new HomeItem.EventItem(e));
            }
            if (!hc.tasks.isEmpty()) {
                add(new HomeItem.Header("Задачи"));
                for (var t : hc.tasks) add(new HomeItem.TaskItem(t));
            }
        }});

        textEmpty.setVisibility(hc.tasks.isEmpty() && hc.events.isEmpty()
                ? View.VISIBLE : View.GONE);
        textAllDone.setVisibility(hc.tasks.stream().allMatch(t -> t.done) && !hc.tasks.isEmpty()
                ? View.VISIBLE : View.GONE);

        homeAdapter.notifyDataSetChanged();
    }

    private void updateDateTime() {
        LocalDate today = LocalDate.now();
        LocalTime time = LocalTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("EEEE, d MMMM", new Locale("ru"));
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        String d = today.format(df);
        d = d.substring(0,1).toUpperCase() + d.substring(1);
        textDayMonth.setText(d);
        textTime.setText(time.format(tf));
    }

    private void setQuoteLoading(boolean isLoading) {
        quoteProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnNewQuote.setEnabled(!isLoading);
    }
}
