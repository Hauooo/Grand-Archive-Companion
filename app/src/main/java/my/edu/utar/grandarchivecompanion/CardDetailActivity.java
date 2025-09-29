package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import io.noties.markwon.Markwon;

public class CardDetailActivity extends AppCompatActivity {

    ImageView cardImage;
    TextView cardName, cardType, cardText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail);

        // Find views
        cardImage = findViewById(R.id.cardImage);
        cardName = findViewById(R.id.cardName);
        cardType = findViewById(R.id.cardType);
        cardText = findViewById(R.id.cardText);

        // Get data from Intent
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String name = getIntent().getStringExtra("name");
        String type = getIntent().getStringExtra("type");
        String text = getIntent().getStringExtra("text");

        // Setup Markwon
        Markwon markwon = Markwon.create(this);

        // Render Markdown properly
        markwon.setMarkdown(cardName, name != null ? name : "");
        cardType.setText(type != null ? type : "");
        markwon.setMarkdown(cardText, text != null ? text : "");

        // Load card image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("/")) {
                imageUrl = "https://api.gatcg.com" + imageUrl;
            }

            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(cardImage);
        } else {
            cardImage.setImageResource(R.drawable.placeholder);
        }
    }
}
