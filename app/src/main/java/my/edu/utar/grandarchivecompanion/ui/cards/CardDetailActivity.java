// File: `app/src/main/java/my/edu/utar/grandarchivecompanion/ui/cards/CardDetailActivity.java`
package my.edu.utar.grandarchivecompanion.ui.cards;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.squareup.picasso.Picasso;
import my.edu.utar.grandarchivecompanion.R;
import my.edu.utar.grandarchivecompanion.databinding.ActivityCardDetailBinding;

import io.noties.markwon.Markwon;
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
                .usePlugin(GlideImagesPlugin.create(this))
                .build();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CardItem card = getIntent().getParcelableExtra("selectedCard");

        if (card != null) {
            binding.cardNameDetail.setText(card.getName());
            binding.cardTypeDetail.setText(card.getType());

            // Parse tokens to <img src="ic_*"/> and render using ResourceImageGetter
            String html = CardsViewModel.EffectTextParser.parseEffectText(card.getEffectRaw());

            //Convert plaintext newlines into HTML line breaks
            if (html != null && !html.isEmpty()) {
                // Split on double newlines into paragraphs
                String[] paragraphs = html.split("\\n\\n");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < paragraphs.length; i++) {
                    String p = paragraphs[i];
                    // Replace any remaining single newlines inside a paragraph with <br/>
                    p = p.replace("\n", "<br/>");
                    sb.append("<p>").append(p).append("</p>");
                }
                html = sb.toString();
            }

            int sizePx = (int) (binding.cardTextDetail.getTextSize() * 1.2f);
            ResourceImageGetter imageGetter = new ResourceImageGetter(this, sizePx);
            binding.cardTextDetail.setText(imageGetter.fromHtml(html));

            binding.cardRulingsDetail.setText(card.getRulings());
            binding.cardLegalityDetail.setText(card.getLegality());

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
