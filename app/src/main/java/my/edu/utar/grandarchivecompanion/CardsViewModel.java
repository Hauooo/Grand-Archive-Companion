package my.edu.utar.grandarchivecompanion;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

    // --- Member Variables ---
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean canLoadMore = true;
    private String currentQuery = "";
    private String currentSetPrefix = "";
    private static final int PAGE_SIZE = 20;

    // --- LiveData ---
    // The single source of truth for the entire list of cards
    private final MutableLiveData<List<CardItem>> _cards = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isError = new MutableLiveData<>(false);

    public LiveData<List<CardItem>> getCards() { return _cards; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Boolean> isLoadingMore() { return _isLoadingMore; }
    public LiveData<Boolean> isError() { return _isError; }

    // --- Initialization ---
    public CardsViewModel() {
        fetchCards();
    }

    // --- Public Methods ---
    /**
     * Resets and fetches the first page of cards, either for the initial load or a new search.
     */
    public void fetchCards() {
        currentPage = 1;
        canLoadMore = true;
        isLoadingMore = false;
        // Use setValue as this is called from the main thread (e.g., UI event)
        _isLoading.setValue(true);
        _isError.setValue(false);
        _cards.setValue(new ArrayList<>()); // Clear the list immediately for a new search
        fetchDataForPage(currentPage);
    }

    /**
     * Fetches the next page of cards if not already loading and if more pages are available.
     */
    public void loadMoreCards() {
        if (isLoadingMore || !canLoadMore) return;
        isLoadingMore = true;
        _isLoadingMore.postValue(true); // Use postValue for thread safety
        currentPage++;
        fetchDataForPage(currentPage);
    }

    /**
     * Sets the search query with a debounce to prevent excessive API calls while typing.
     */
    public void setSearchQuery(String query) {
        searchHandler.removeCallbacks(searchRunnable);
        searchRunnable = () -> {
            currentQuery = query;
            fetchCards();
        };
        searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
    }

    public void setSetPrefix(String prefix){
        if (currentSetPrefix.equals(prefix))
            return;
        currentSetPrefix = prefix;
        fetchCards();
    }

    // --- Private Helper Methods ---
    private void fetchDataForPage(final int page) {
        String baseUrl = "https://api.gatcg.com/cards/search";
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?page=").append(page);
        urlBuilder.append("&limit=").append(PAGE_SIZE);

        if (!currentQuery.isEmpty()) {
            urlBuilder.append("&name=").append(currentQuery);
        }
        if (!currentSetPrefix.isEmpty()) {
            urlBuilder.append("&prefix=").append(currentSetPrefix);
        }
        String finalUrl = urlBuilder.toString();
        Request request = new Request.Builder().url(finalUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                isLoadingMore = false;
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
                if (page == 1) {
                    _isError.postValue(true);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JsonArray dataArray = gson.fromJson(json, JsonObject.class).getAsJsonArray("data");
                    List<CardItem> newCards = parseCards(dataArray);

                    if (newCards.size() < PAGE_SIZE) {
                        canLoadMore = false;
                    }

                    // CRITICAL FIX: Perform LiveData update on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (page == 1) {
                            _cards.setValue(newCards);
                        } else {
                            List<CardItem> currentList = _cards.getValue();
                            if (currentList == null) {
                                currentList = new ArrayList<>();
                            }
                            List<CardItem> updatedList = new ArrayList<>(currentList);
                            updatedList.addAll(newCards);
                            _cards.setValue(updatedList);
                        }
                    });
                } else {
                    onFailure(call, new IOException("Unexpected API response: " + response));
                }
                isLoadingMore = false;
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
            }
        });
    }

    /**
     * Safely parses a JSON array into a list of CardItem objects.
     */


    private List<CardItem> parseCards(JsonArray dataArray) {
        List<CardItem> newCards = new ArrayList<>();
        if (dataArray == null) return newCards;

        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject cardObj = dataArray.get(i).getAsJsonObject();
            String name = cardObj.has("name") ? cardObj.get("name").getAsString() : "Unknown";

            // --- (Your existing, correct parsing logic for other fields) ---
            String imageUrl = "";
            if (cardObj.has("editions") && !cardObj.get("editions").isJsonNull() && cardObj.get("editions").isJsonArray()) {
                JsonArray editions = cardObj.getAsJsonArray("editions");
                if (editions.size() > 0) {
                    JsonObject firstEdition = editions.get(0).getAsJsonObject();
                    if (firstEdition.has("image") && !firstEdition.get("image").isJsonNull()) {
                        imageUrl = "https://api.gatcg.com" + firstEdition.get("image").getAsString();
                    }
                }
            }
            String type = "Unknown Type";
            if (cardObj.has("types") && !cardObj.get("types").isJsonNull() && cardObj.get("types").isJsonArray()) {
                JsonArray types = cardObj.getAsJsonArray("types");
                if (types.size() > 0) {
                    type = types.get(0).getAsString();
                }
            }
            String text = cardObj.has("effect_raw") && !cardObj.get("effect_raw").isJsonNull() ?
                    cardObj.get("effect_raw").getAsString() : "No effect text.";

            // --- ⭐ CORRECTED RULINGS PARSING LOGIC ---
            StringBuilder rulingsBuilder = new StringBuilder();
            if (cardObj.has("rule") && !cardObj.get("rule").isJsonNull() && cardObj.get("rule").isJsonArray()) {
                JsonArray rulingsArray = cardObj.getAsJsonArray("rule");
                for (int j = 0; j < rulingsArray.size(); j++) {
                    JsonObject rulingObj = rulingsArray.get(j).getAsJsonObject();
                    String rulingText = "";
                    // First, check for the "text" key
                    if (rulingObj.has("text") && !rulingObj.get("text").isJsonNull()) {
                        rulingText = rulingObj.get("text").getAsString();
                    }
                    // If "text" isn't found, check for the "description" key
                    else if (rulingObj.has("description") && !rulingObj.get("description").isJsonNull()) {
                        rulingText = rulingObj.get("description").getAsString();
                    }

                    if (!rulingText.isEmpty()) {
                        rulingsBuilder.append("• ").append(rulingText).append("\n\n");
                    }
                }
            }
            String rulingsText = rulingsBuilder.length() > 0 ? rulingsBuilder.toString().trim() : "No rulings.";

            // --- (Your existing, correct legality parsing logic) ---
            StringBuilder legalityBuilder = new StringBuilder();
            boolean isCardBanned = false;
            if (cardObj.has("legality") && !cardObj.get("legality").isJsonNull() && cardObj.get("legality").isJsonObject()) {
                JsonObject legalityObj = cardObj.getAsJsonObject("legality");
                for (String formatKey : legalityObj.keySet()) {
                    JsonElement statusElement = legalityObj.get(formatKey);
                    String status = "Unknown";
                    if (statusElement != null) {
                        if (statusElement.isJsonObject()) {
                            JsonObject statusObj = statusElement.getAsJsonObject();
                            if (statusObj.has("status") && !statusObj.get("status").isJsonNull()) {
                                status = statusObj.get("status").getAsString();
                            } else if (statusObj.has("limit") && !statusObj.get("limit").isJsonNull()) {
                                int limit = statusObj.get("limit").getAsInt();
                                status = (limit == 0) ? "Banned" : "Legal";
                            }
                        } else if (statusElement.isJsonPrimitive()) {
                            status = statusElement.getAsString();
                        }

                        if (status.equalsIgnoreCase("Banned")){
                            isCardBanned = true;
                        }
                    }
                    String formattedFormat = Character.toUpperCase(formatKey.charAt(0)) + formatKey.substring(1).toLowerCase();
                    String formattedStatus = Character.toUpperCase(status.charAt(0)) + status.substring(1).toLowerCase();
                    legalityBuilder.append(formattedFormat).append(": ").append(formattedStatus).append("\n");
                }
            } else {
                legalityBuilder.append("Standard: Legal\n");
                legalityBuilder.append("Draft: Legal");
            }
            String legalityText = legalityBuilder.toString().trim();

            newCards.add(new CardItem(name, imageUrl, type, text, rulingsText, legalityText, isCardBanned));
        }
        return newCards;
    }
}