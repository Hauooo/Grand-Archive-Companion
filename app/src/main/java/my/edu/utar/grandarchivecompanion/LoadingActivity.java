package my.edu.utar.grandarchivecompanion;

import android.content.Intent;
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
    private Call apiCall;

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
        apiCall = client.newCall(request);
        apiCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> statusText.setText("Failed to load: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();

                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    intent.putExtra("card_data", json);
                    startActivity(intent);
                    finish();
                } else {
                    runOnUiThread(() -> statusText.setText("Error: " + response.code()));
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiCall != null) {
            apiCall.cancel(); // Cancel the call if the activity is destroyed
        }
    }
}

