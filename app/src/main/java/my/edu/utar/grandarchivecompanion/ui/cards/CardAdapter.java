package my.edu.utar.grandarchivecompanion.ui.cards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

import my.edu.utar.grandarchivecompanion.R;

public class CardAdapter extends ListAdapter<CardItem, RecyclerView.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CardItem card);
    }

    public CardAdapter(@NonNull OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_row, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CardItem card = getItem(position);
        ((CardViewHolder) holder).bind(card, listener);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardName, cardType, cardText;
        ImageView cardImage;
        private final android.content.res.ColorStateList defaultTextColor;
        private final Markwon markwon;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardType = itemView.findViewById(R.id.card_type);
            cardText = itemView.findViewById(R.id.card_effect_text);
            cardImage = itemView.findViewById(R.id.card_image);
            defaultTextColor = cardName.getHintTextColors();

            markwon = Markwon.builder(itemView.getContext())
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(ImagesPlugin.create())
                    .usePlugin(GlideImagesPlugin.create(itemView.getContext()))
                    .build();
        }

        void bind(final CardItem card, final OnItemClickListener listener) {
            cardName.setText(card.getName());
            cardType.setText(card.getType());
            // Use Markwon to render effect text (supports HTML and images via plugins)
            markwon.setMarkdown(cardText, card.getEffectRaw());

            if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                Picasso.get().load(card.getImageUrl()).into(cardImage);
            } else {
                cardImage.setImageResource(android.R.color.darker_gray);
            }

            if (card.isBanned()) {
                cardName.setTextColor(itemView.getContext().getResources().getColor(R.color.banned_red));
            } else {
                cardName.setTextColor(defaultTextColor);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(card);
                }
            });
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static final DiffUtil.ItemCallback<CardItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
