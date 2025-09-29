package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class LoadingActivity extends AppCompatActivity {

    private TextView statusText;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        statusText = findViewById(R.id.statusText);

        // Start fetching data
        fetchCards();
    }

    private void fetchCards() {
        String url = "https://api.gatcg.com/cards/search"; // <-- replace with your DB link

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Asynchronous request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> statusText.setText("Failed to load: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        statusText.setText("Data Loaded!");
                        // TODO: parse JSON and go to main screen
                    });
                } else {
                    runOnUiThread(() -> statusText.setText("Error: " + response.code()));
                }
            }
        });
    }
}

