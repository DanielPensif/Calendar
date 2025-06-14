package com.example.Kalendar.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.Kalendar.domain.GetHomeContentUseCase;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private final GetHomeContentUseCase uc;
    private final ExecutorService ex = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> quoteLive = new MutableLiveData<>();

    @Inject
    public HomeViewModel(GetHomeContentUseCase uc) { this.uc = uc; }

    public LiveData<HomeContent> loadToday(int userId) {
        return uc.execute(userId);
    }
    public LiveData<String> getQuote() {
        fetchQuote();
        return quoteLive;
    }

    private void fetchQuote() {
        ex.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            String current = quoteLive.getValue();
            Request req = new Request.Builder()
                    .url("http://api.forismatic.com/api/1.0/?method=getQuote&format=json&lang=ru")
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String text = "Не удалось загрузить цитату";
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        try {
                            String q = new JSONObject(body).getString("quoteText").trim();
                            text = q.equals(current) ? q : q;
                        } catch (Exception ignored) {}
                    }
                    quoteLive.postValue(text);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    quoteLive.postValue("Не удалось загрузить цитату");
                }
            });
        });
    }
}
