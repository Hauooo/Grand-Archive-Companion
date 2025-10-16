package my.edu.utar.grandarchivecompanion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import io.noties.markwon.Markwon;
import my.edu.utar.grandarchivecompanion.databinding.ActivityCardDetailBinding; // 1. Import View Binding class

public class CardDetailActivity extends AppCompatActivity {
    // The key for the single Parcelable object
    public static final String EXTRA_CARD_ITEM = "my.edu.utar.grandarchivecompanion.EXTRA_CARD_ITEM";

    // 2. Declare the binding object, replacing individual View variables
    private ActivityCardDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 3. Inflate the layout using the binding class
        binding = ActivityCardDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 4. Get the single CardItem object from the intent
        CardItem card = getIntent().getParcelableExtra(EXTRA_CARD_ITEM);

        // This check is now correct because 'card' is declared
        if (card == null) {
            finish(); // Exit if no card data is found
            return;
        }

        // Setup Markwon
        Markwon markwon = Markwon.create(this);

        // 5. Use the 'binding' object and 'card' object for ALL views and data
        markwon.setMarkdown(binding.cardNameDetail, card.getName());
        binding.cardTypeDetail.setText(card.getType());
        markwon.setMarkdown(binding.cardTextDetail, card.getText());
        markwon.setMarkdown(binding.cardRulingsDetail, card.getRulings());
        binding.cardLegalityDetail.setText(card.getLegality());
        binding.cardImageDetail.setContentDescription("Image of the card: " + card.getName());


        if (card.isBanned()){
            binding.cardLegalityDetail.setTextColor(getColor(R.color.banned_red));
        }
        // Load card image using the URL from the 'card' object
        String imageUrl = card.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(binding.cardImageDetail); // Use the binding object here
        } else {
            binding.cardImageDetail.setImageResource(R.drawable.placeholder);
        }
    }
}