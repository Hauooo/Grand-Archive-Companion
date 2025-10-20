package my.edu.utar.grandarchivecompanion.ui.cards;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil; // --- FIX 1: Import DataBindingUtil ---

import com.squareup.picasso.Picasso;
import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.ActivityCardDetailBinding;

public class CardDetailActivity extends AppCompatActivity {

    private ActivityCardDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- FIX 2: Use DataBindingUtil to set the content view ---
        // This correctly inflates the layout and sets the content view in one step
        // for layouts wrapped with the <layout> tag.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_detail);

        // Your original inflate/setContentView lines are no longer needed:
        // binding = ActivityCardDetailBinding.inflate(getLayoutInflater());
        // setContentView(binding.getRoot()); // This was the line causing the error

        // Enable the back arrow in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the CardItem from the Intent
        CardItem card = getIntent().getParcelableExtra("selectedCard");

        if (card != null) {
            // Populate the views (this part of your code is correct)
            binding.cardNameDetail.setText(card.getName());
            binding.cardTypeDetail.setText(card.getType());
            binding.cardTextDetail.setText(card.getText());
            binding.cardRulingsDetail.setText(card.getRulings());
            binding.cardLegalityDetail.setText(card.getLegality());

            // Load image using Picasso
            String imageUrl = card.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(new ColorDrawable(Color.LTGRAY))
                        .error(new ColorDrawable(Color.DKGRAY))
                        .into(binding.cardImageDetail);
            } else {
                binding.cardImageDetail.setImageDrawable(new ColorDrawable(Color.LTGRAY));
            }
        }
    }

    // Handle toolbar back button press
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes this activity and returns to MainActivity
        return true;
    }
}
