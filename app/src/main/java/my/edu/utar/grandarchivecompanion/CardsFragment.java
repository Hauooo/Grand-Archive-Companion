package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class CardsFragment extends Fragment {

    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private RecyclerView recyclerView;
    private CardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CardAdapter(getContext());
        recyclerView.setAdapter(adapter);

        fetchCards();

        return view;
    }

    private void fetchCards() {
        String url = "https://api.gatcg.com/cards/search";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();

                    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                    JsonArray dataArray = jsonObject.getAsJsonArray("data");

                    List<CardItem> newCards = new ArrayList<>();

                    for (int i = 0; i < dataArray.size(); i++) {
                        JsonObject cardObj = dataArray.get(i).getAsJsonObject();
                        String name = cardObj.get("name").getAsString();

                        String imagePath = null;
                        if (cardObj.has("editions") && cardObj.getAsJsonArray("editions").size() > 0) {
                            JsonObject firstEdition = cardObj.getAsJsonArray("editions").get(0).getAsJsonObject();
                            if (firstEdition.has("image") && !firstEdition.get("image").isJsonNull()) {
                                imagePath = firstEdition.get("image").getAsString();
                            }
                        }

                        String imageUrl = imagePath != null ? "https://api.gatcg.com" + imagePath : "";
                        String type = "Unknown";
                        if (cardObj.has("types") && !cardObj.get("types").isJsonNull()) {
                            JsonArray typesArray = cardObj.getAsJsonArray("types");
                            List<String> typeList = new ArrayList<>();
                            for (int j = 0; j < typesArray.size(); j++) {
                                typeList.add(typesArray.get(j).getAsString());
                            }
                            type = String.join(", ", typeList);
                        }

                        String text = cardObj.has("effect") && !cardObj.get("effect").isJsonNull() ?
                                cardObj.get("effect").getAsString() : "No description available.";

                        newCards.add(new CardItem(name, imageUrl, type, text));
                    }

                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        adapter.updateList(newCards);
                    });
                }
            }
        });
    }
}