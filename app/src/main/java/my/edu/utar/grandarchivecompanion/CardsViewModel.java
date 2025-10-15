package my.edu.utar.grandarchivecompanion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CardsViewModel extends ViewModel {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean canLoadMore = true;
    private String currentQuery = "";
    private static final int PAGE_SIZE = 20;

    private final MutableLiveData<List<CardItem>> _cards = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<CardItem>> _newCardsPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isError = new MutableLiveData<>(false);

    public LiveData<List<CardItem>> getCards() { return _cards; }
    public LiveData<List<CardItem>> getNewCardsPage() { return _newCardsPage; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Boolean> isLoadingMore() { return _isLoadingMore; }
    public LiveData<Boolean> isError() { return _isError; }

    public CardsViewModel() {
        fetchCards();
    }

    public void fetchCards() {
        currentPage = 1;
        canLoadMore = true;
        _isLoading.setValue(true);
        _isError.setValue(false);
        _cards.setValue(new ArrayList<>());
        fetchDataForPage(currentPage);
    }

    public void loadMoreCards() {
        if (isLoadingMore || !canLoadMore) return;
        isLoadingMore = true;
        _isLoadingMore.postValue(true);
        currentPage++;
        fetchDataForPage(currentPage);
    }

    public void setSearchQuery(String query) {
        currentQuery = query;
        fetchCards(); // Trigger a new search
    }

    private void fetchDataForPage(final int page) {
        String url = "https://api.gatcg.com/cards/search?q=" + currentQuery + "&page=" + page + "&limit=" + PAGE_SIZE;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (page == 1) _isError.postValue(true);
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
                isLoadingMore = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JsonArray dataArray = gson.fromJson(json, JsonObject.class).getAsJsonArray("data");
                    List<CardItem> newCards = parseCards(dataArray);

                    if (newCards.size() < PAGE_SIZE) canLoadMore = false;

                    // --- THIS IS THE KEY LOGICAL FIX ---
                    if (page == 1) {
                        // For the first page, post to the main _cards LiveData
                        _cards.postValue(newCards);
                    } else {
                        // For subsequent pages, post ONLY the new page to the _newCardsPage LiveData
                        _newCardsPage.postValue(newCards);
                    }

                    // ... (rest of the state updates are the same)
                    _isLoading.postValue(false);
                    _isLoadingMore.postValue(false);
                    isLoadingMore = false;
                } else {
                    onFailure(call, new IOException("Unexpected code " + response));
                }
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
                isLoadingMore = false;
            }
        });
    }

    private List<CardItem> parseCards(JsonArray dataArray) {
        List<CardItem> newCards = new ArrayList<>();
        if (dataArray == null) return newCards;

        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject cardObj = dataArray.get(i).getAsJsonObject();
            String name = cardObj.has("name") ? cardObj.get("name").getAsString() : "Unknown";
            String imageUrl = "";
            if (cardObj.has("editions") && cardObj.getAsJsonArray("editions").size() > 0) {
                JsonObject firstEdition = cardObj.getAsJsonArray("editions").get(0).getAsJsonObject();
                if (firstEdition.has("image") && !firstEdition.get("image").isJsonNull()) {
                    imageUrl = "https://api.gatcg.com" + firstEdition.get("image").getAsString();
                }
            }
            String type = "Unknown";
            if (cardObj.has("types") && cardObj.getAsJsonArray("types").size() > 0) {
                type = cardObj.getAsJsonArray("types").get(0).getAsString();
            }
            String text = cardObj.has("effect_raw") && !cardObj.get("effect_raw").isJsonNull() ?
                    cardObj.get("effect_raw").getAsString() : "No effect text.";
            newCards.add(new CardItem(name, imageUrl, type, text));
        }
        return newCards;
    }
}