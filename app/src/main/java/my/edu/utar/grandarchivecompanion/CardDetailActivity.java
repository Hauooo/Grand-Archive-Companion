package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import io.noties.markwon.Markwon;

public class CardDetailActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL = "my.edu.utar.grandarchivecompanion.EXTRA_IMAGE_URL";
    public static final String EXTRA_NAME = "my.edu.utar.grandarchivecompanion.EXTRA_NAME";
    public static final String EXTRA_TYPE = "my.edu.utar.grandarchivecompanion.EXTRA_TYPE";
    public static final String EXTRA_TEXT = "my.edu.utar.grandarchivecompanion.EXTRA_TEXT";

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
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String text = getIntent().getStringExtra(EXTRA_TEXT);

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
