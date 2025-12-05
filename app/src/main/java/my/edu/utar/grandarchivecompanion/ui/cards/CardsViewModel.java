package my.edu.utar.grandarchivecompanion.ui.cards;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    // --- Search & Filter State ---
    private String currentQuery = "";
    private String currentSetPrefix = "";
    private String currentElement = "";
    private String currentClass = "";
    private String currentType = "";
    private String currentSubtype = "";

    private static final int PAGE_SIZE = 20;

    // --- LiveData ---
    private final MutableLiveData<List<CardItem>> _cards = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isError = new MutableLiveData<>(false);

    public LiveData<List<CardItem>> getCards() { return _cards; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Boolean> isLoadingMore() { return _isLoadingMore; }
    public LiveData<Boolean> isError() { return _isError; }

    public CardsViewModel() {
        fetchCards();
    }

    // --- Public Methods ---

    public void fetchCards() {
        currentPage = 1;
        canLoadMore = true;
        isLoadingMore = false;
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
        searchHandler.removeCallbacks(searchRunnable);
        searchRunnable = () -> {
            currentQuery = query;
            fetchCards();
        };
        searchHandler.postDelayed(searchRunnable, 500);
    }

    public void setSetPrefix(String prefix) {
        if (currentSetPrefix.equals(prefix)) return;
        currentSetPrefix = prefix;
        fetchCards();
    }

    public void updateFilters(String element, String cardClass, String type, String subtype) {
        this.currentElement = element != null ? element : "";
        this.currentClass = cardClass != null ? cardClass : "";
        this.currentType = type != null ? type : "";
        this.currentSubtype = subtype != null ? subtype : "";
        fetchCards();
    }

    // --- Private Helper Methods ---

    private void fetchDataForPage(final int page) {
        String baseUrl = "https://api.gatcg.com/cards/search";
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?page=").append(page);
        urlBuilder.append("&limit=").append(PAGE_SIZE);

        if (!currentQuery.isEmpty()) urlBuilder.append("&name=").append(currentQuery);
        if (!currentSetPrefix.isEmpty()) urlBuilder.append("&prefix=").append(currentSetPrefix);
        if (!currentElement.isEmpty()) urlBuilder.append("&element=").append(currentElement);
        if (!currentClass.isEmpty()) urlBuilder.append("&class=").append(currentClass);
        if (!currentType.isEmpty()) urlBuilder.append("&type=").append(currentType);
        if (!currentSubtype.isEmpty()) urlBuilder.append("&subtype=").append(currentSubtype);

        Request request = new Request.Builder().url(urlBuilder.toString()).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                isLoadingMore = false;
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
                if (page == 1) _isError.postValue(true);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JsonObject rootObj = gson.fromJson(json, JsonObject.class);
                        List<CardItem> newCards = new ArrayList<>();

                        if (rootObj.has("data") && rootObj.get("data").isJsonArray()) {
                            JsonArray dataArray = rootObj.getAsJsonArray("data");
                            newCards = parseCards(dataArray, currentSetPrefix);
                        }

                        if (newCards.size() < PAGE_SIZE) canLoadMore = false;

                        List<CardItem> finalNewCards = newCards;
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (page == 1) {
                                _cards.setValue(finalNewCards);
                            } else {
                                List<CardItem> currentList = _cards.getValue();
                                if (currentList == null) currentList = new ArrayList<>();
                                List<CardItem> updatedList = new ArrayList<>(currentList);
                                updatedList.addAll(finalNewCards);
                                _cards.setValue(updatedList);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (page == 1) _isError.postValue(true);
                    }
                } else {
                    if (page == 1) _isError.postValue(true);
                }
                isLoadingMore = false;
                _isLoading.postValue(false);
                _isLoadingMore.postValue(false);
            }
        });
    }

    private List<CardItem> parseCards(JsonArray dataArray, String activeSetPrefix) {
        List<CardItem> newCards = new ArrayList<>();
        if (dataArray == null) return newCards;
        for (int i = 0; i < dataArray.size(); i++) {
            // Start parsing with isNested = false
            CardItem item = parseSingleCard(dataArray.get(i).getAsJsonObject(), activeSetPrefix, false);
            if (item != null) newCards.add(item);
        }
        return newCards;
    }

    @Nullable
    private CardItem parseSingleCard(@NonNull JsonObject cardObj, String activeSetPrefix, boolean isNested) {

        // --- 1. Other Orientations (Parsed First) ---
        List<CardItem> otherOrientations = new ArrayList<>();

        // Only parse other orientations if we are NOT currently inside a nested object to avoid infinite recursion
        if (!isNested && cardObj.has("other_orientations") && cardObj.get("other_orientations").isJsonArray()) {
            for (JsonElement e : cardObj.getAsJsonArray("other_orientations")) {
                if (e.isJsonObject()) {
                    // Recursive call: pass true for isNested
                    CardItem other = parseSingleCard(e.getAsJsonObject(), activeSetPrefix, true);
                    if (other != null) otherOrientations.add(other);
                }
            }
        }

        // --- 2. Main Card Data Parsing ---
        String name = cardObj.has("name") ? cardObj.get("name").getAsString() : "Unknown";

        // Image Parsing
        String imageUrl = "";
        // Try Ed. Array (Main cards)
        if (cardObj.has("editions") && cardObj.get("editions").isJsonArray()) {
            JsonArray editions = cardObj.getAsJsonArray("editions");
            if (!activeSetPrefix.isEmpty()) {
                for (JsonElement ed : editions) {
                    JsonObject editionObj = ed.getAsJsonObject();
                    if (editionObj.has("set") && editionObj.get("set").getAsJsonObject().has("prefix")) {
                        String prefix = editionObj.get("set").getAsJsonObject().get("prefix").getAsString();
                        if (prefix.equalsIgnoreCase(activeSetPrefix) && editionObj.has("image")) {
                            imageUrl = "https://api.gatcg.com" + editionObj.get("image").getAsString();
                            break;
                        }
                    }
                }
            }
            if (imageUrl.isEmpty() && editions.size() > 0 && editions.get(0).getAsJsonObject().has("image")) {
                imageUrl = "https://api.gatcg.com" + editions.get(0).getAsJsonObject().get("image").getAsString();
            }
        }
        // Try Ed. Object (Nested cards)
        if (imageUrl.isEmpty() && cardObj.has("edition") && cardObj.get("edition").isJsonObject()) {
            JsonObject edObj = cardObj.getAsJsonObject("edition");
            if (edObj.has("image")) {
                imageUrl = "https://api.gatcg.com" + edObj.get("image").getAsString();
            }
        }
        // Try Direct Image (Fallback)
        if (imageUrl.isEmpty() && cardObj.has("image")) {
            String raw = cardObj.get("image").getAsString();
            imageUrl = raw.startsWith("http") ? raw : "https://api.gatcg.com" + raw;
        }

        // --- Rich Type Parsing ---
        List<String> combinedTypes = new ArrayList<>();

        // Classes parsing logic REMOVED.

        // Types
        if (cardObj.has("types") && cardObj.get("types").isJsonArray()) {
            JsonArray arr = cardObj.getAsJsonArray("types");
            for(JsonElement e : arr) combinedTypes.add(capitalize(e.getAsString()));
        } else if (cardObj.has("type") && !cardObj.get("type").isJsonNull()) {
            combinedTypes.add(capitalize(cardObj.get("type").getAsString()));
        }

        // Subtypes
        if (cardObj.has("subtypes") && cardObj.get("subtypes").isJsonArray()) {
            JsonArray arr = cardObj.getAsJsonArray("subtypes");
            for(JsonElement e : arr) combinedTypes.add(capitalize(e.getAsString()));
        }

        String finalTypeString = TextUtils.join(" - ", combinedTypes);
        if (finalTypeString.isEmpty()) finalTypeString = "Unknown Type";

        // --- Cost Parsing ---
        String cost = "N/A";
        if (cardObj.has("cost") && !cardObj.get("cost").isJsonNull()) cost = cardObj.get("cost").getAsString();
        else if (cardObj.has("cost_memory") && !cardObj.get("cost_memory").isJsonNull()) cost = cardObj.get("cost_memory").getAsString();
        else if (cardObj.has("level") && !cardObj.get("level").isJsonNull()) cost = "Lv." + cardObj.get("level").getAsString();

        // --- Text & Rulings ---
        String rawEffect = extractEffectText(cardObj);

        // Replace CARDNAME with the current card object's name
        if (!name.equals("Unknown") && rawEffect.contains("CARDNAME")) {
            rawEffect = rawEffect.replace("CARDNAME", name);
        }

        String formattedEffect = EffectTextParser.parseEffectText(rawEffect);

        StringBuilder rulingsBuilder = new StringBuilder();
        if (cardObj.has("rule") && cardObj.get("rule").isJsonArray()) {
            for (JsonElement e : cardObj.getAsJsonArray("rule")) {
                JsonObject r = e.getAsJsonObject();
                String txt = r.has("text") ? r.get("text").getAsString() : (r.has("description") ? r.get("description").getAsString() : "");
                if (!txt.isEmpty()) rulingsBuilder.append("• ").append(txt).append("\n\n");
            }
        }
        String rulingsText = rulingsBuilder.length() > 0 ? rulingsBuilder.toString().trim() : "No rulings.";

        // --- Legality ---
        String legalityText = parseLegality(cardObj);
        boolean isBanned = legalityText.contains("Banned");

        return new CardItem(name, imageUrl, cost, finalTypeString, formattedEffect, rulingsText, legalityText, isBanned, otherOrientations);
    }

    // --- Helpers ---

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String parseLegality(JsonObject cardObj) {
        if (!cardObj.has("legality") || !cardObj.get("legality").isJsonObject()) return "Standard: Legal\nDraft: Legal";
        StringBuilder sb = new StringBuilder();
        JsonObject leg = cardObj.getAsJsonObject("legality");
        for (String key : leg.keySet()) {
            String status = "Unknown";
            JsonElement el = leg.get(key);
            if (el.isJsonObject()) {
                JsonObject o = el.getAsJsonObject();
                if (o.has("status")) status = o.get("status").getAsString();
                else if (o.has("limit")) status = o.get("limit").getAsInt() == 0 ? "Banned" : "Legal";
            } else if (el.isJsonPrimitive()) {
                status = el.getAsString();
            }
            sb.append(capitalize(key)).append(": ").append(capitalize(status)).append("\n");
        }
        return sb.toString().trim();
    }

    private String extractEffectText(JsonObject cardObj) {
        // Priority: effect > effect_raw > others
        String[] keys = {"effect", "effect_raw", "effect_text", "text", "effect_html"};
        for (String key : keys) {
            if (cardObj.has(key) && !cardObj.get(key).isJsonNull()) {
                JsonElement el = cardObj.get(key);
                if (el.isJsonPrimitive()) return el.getAsString();
                if (el.isJsonArray()) {
                    StringBuilder sb = new StringBuilder();
                    for(JsonElement e : el.getAsJsonArray()) sb.append(e.getAsString()).append("\n\n");
                    return sb.toString().trim();
                }
            }
        }
        return "";
    }

    public static final class EffectTextParser {
        private EffectTextParser() {}
        public static String parseEffectText(String rawText) {
            if (rawText == null) return "";
            return rawText.replaceAll("(?<!\\w)\\[?POWER\\]?(?!\\w)", "<img src=\"ic_sword\"/>")
                    .replaceAll("(?<!\\w)\\[?LIFE\\]?(?!\\w)", "<img src=\"ic_heart\"/>")
                    .replaceAll("(?<!\\w)\\[?REST\\]?(?!\\w)", "<img src=\"ic_rest\"/>");
        }
    }
}