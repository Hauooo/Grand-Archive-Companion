package my.edu.utar.grandarchivecompanion.ui.cards;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

import java.util.ArrayList;
import java.util.List;

import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.ActivityCardDetailBinding;

public class CardDetailActivity extends AppCompatActivity {

    private ActivityCardDetailBinding binding;
    private CardItem originalCard;
    private List<CardItem> allOrientations;
    private int currentOrientationIndex = 0;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_detail);

        // Initialize Markwon with Glide for inline images (element icons, etc.)
        markwon = Markwon.builder(this)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(GlideImagesPlugin.create(this)) // Uses Glide context
                .build();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve the card object
        originalCard = getIntent().getParcelableExtra("selectedCard");

        if (originalCard != null) {
            // Build orientation list
            allOrientations = new ArrayList<>();
            List<CardItem> others = originalCard.getOtherOrientations();

            // LOGIC CHANGE: Prioritize other_orientations if they exist.
            // This fetches/displays the data from the "other" orientation first, instead of the current one.
            if (others != null && !others.isEmpty()) {
                allOrientations.addAll(others);
                allOrientations.add(originalCard); // Add original card last (so it becomes the 'flip' side)
            } else {
                allOrientations.add(originalCard);
            }

            // Display the first item in the list (which is now the other orientation if it existed)
            if (!allOrientations.isEmpty()) {
                displayCard(allOrientations.get(0));
            }

            // Activate "Flip" button if multiple orientations exist
            if (allOrientations.size() > 1) {
                binding.flipToOtherOrientation.setVisibility(View.VISIBLE);
                binding.flipToOtherOrientation.setOnClickListener(v -> {
                    currentOrientationIndex = (currentOrientationIndex + 1) % allOrientations.size();
                    displayCard(allOrientations.get(currentOrientationIndex));
                });
            } else {
                binding.flipToOtherOrientation.setVisibility(View.GONE);
            }
        }
    }

    private void displayCard(CardItem card) {
        if (card == null) return;

        binding.cardNameDetail.setText(card.getName());
        binding.cardTypeDetail.setText(card.getType());
        binding.cardTextDetail.setTextSize(20f);

        // The card object from ViewModel already has 'CARDNAME' replaced and symbols parsed.
        String effectText = card.getEffectRaw();

        if (effectText != null && !effectText.isEmpty()) {
            // Markwon renders the pre-formatted Markdown/HTML
            markwon.setMarkdown(binding.cardTextDetail, effectText);
        } else {
            binding.cardTextDetail.setText("");
        }

        binding.cardRulingsDetail.setText(card.getRulings());
        binding.cardLegalityDetail.setText(card.getLegality());

        String imageUrl = card.getImageUrl();

        // Use Glide for the main card image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .error(new ColorDrawable(Color.DKGRAY))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .fitCenter()
                    .into(binding.cardImageDetail);
        } else {
            binding.cardImageDetail.setImageDrawable(new ColorDrawable(Color.LTGRAY));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}