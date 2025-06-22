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
import okhttp3.*;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final int MAX_ATTEMPTS = 15;
    private final GetHomeContentUseCase uc;
    private final ExecutorService ex = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> quoteLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLive = new MutableLiveData<>(false);

    @Inject
    public HomeViewModel(GetHomeContentUseCase uc) { this.uc = uc; }

    public LiveData<HomeContent> loadToday(int userId) {
        return uc.execute(userId);
    }
    public LiveData<String> getQuote() {
        fetchUniqueQuote();
        return quoteLive;
    }

    public LiveData<Boolean> isQuoteLoading() {
        return loadingLive;
    }

    private void fetchUniqueQuote() {
        ex.execute(() -> {
            loadingLive.postValue(true);
            OkHttpClient client = new OkHttpClient();
            String current = quoteLive.getValue();
            String result = "Не удалось загрузить цитату";

            Request baseRequest = new Request.Builder()
                    .url("http://api.forismatic.com/api/1.0/?method=getQuote&format=json&lang=ru")
                    .build();

            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                try {
                    Response response = client.newCall(baseRequest).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        String q = new org.json.JSONObject(body)
                                .getString("quoteText").trim();
                        if (!q.isEmpty() && !q.equals(current)) {
                            result = q;
                            break;
                        }
                    }
                } catch (IOException | org.json.JSONException ignored) {
                }
            }

            quoteLive.postValue(result);
            loadingLive.postValue(false);
        });
    }
}
