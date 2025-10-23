package my.edu.utar.grandarchivecompanion.ui.cards;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil; // --- FIX 1: Import DataBindingUtil ---

import com.squareup.picasso.Picasso;
import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.ActivityCardDetailBinding;
import io.noties.markwon.Markwon; // --- FIX 2: Import Markwon ---
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;



public class CardDetailActivity extends AppCompatActivity {

    private ActivityCardDetailBinding binding;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_detail);

        markwon = Markwon.builder(this)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(GlideImagesPlugin.create(this)) // <-- ADDED a parenthesis here
                .build();
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
            markwon.setMarkdown(binding.cardTextDetail, card.getEffectRaw());
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
