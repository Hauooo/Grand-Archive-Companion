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

import java.util.List;

public class CardAdapter extends ListAdapter<CardItem, RecyclerView.ViewHolder> {

    private final OnItemClickListener listener;

    // --- Define View Type Constants ---
    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    public interface OnItemClickListener {
        void onItemClick(CardItem card);
    }

    public CardAdapter(@NonNull OnItemClickListener listener) {
        // We use the new DIFF_CALLBACK that handles null/loading items
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // --- 1. Override getItemViewType to return the correct type ---
    @Override
    public int getItemViewType(int position) {
        // If getItem(position) is null, it means it's the loading item
        return getItem(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_CARD;
    }

    // --- 2. Override onCreateViewHolder to inflate the correct layout ---
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CARD) {
            View view = inflater.inflate(R.layout.item_card_grid, parent, false);
            return new CardViewHolder(view);
        } else { // VIEW_TYPE_LOADING
            View view = inflater.inflate(R.layout.item_loading, parent, false); // <--- ASSUME you have a layout named item_loading.xml
            return new LoadingViewHolder(view);
        }
    }

    // --- 3. Override onBindViewHolder to cast and bind the correct ViewHolder ---
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_CARD) {
            CardItem card = getItem(position);
            // getItem() can return null here, but we already checked in getItemViewType
            if (card != null) {
                ((CardViewHolder) holder).bind(card, listener);
            }
        }
        // LoadingViewHolder doesn't need a bind method unless you have custom logic
    }

    // --- CardViewHolder (No Change) ---
    static class CardViewHolder extends RecyclerView.ViewHolder {
        // ... (existing implementation is fine) ...
        TextView cardName;
        ImageView cardImage;
        private final android.content.res.ColorStateList defaultTextColor;
        private final Markwon markwon;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardImage = itemView.findViewById(R.id.card_image);
            defaultTextColor = cardName.getHintTextColors();

            markwon = Markwon.builder(itemView.getContext())
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(ImagesPlugin.create())
                    // Note: GlideImagesPlugin dependency is required for this line to work
                    .usePlugin(GlideImagesPlugin.create(itemView.getContext()))
                    .build();
        }

        void bind(final CardItem card, final OnItemClickListener listener) {
            cardName.setText(card.getName());

            if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                Picasso.get().load(card.getImageUrl()).into(cardImage);
            } else {
                cardImage.setImageResource(android.R.color.darker_gray);
            }

            if (card.isBanned()) {
                // Ensure R.color.banned_red exists
                cardName.setTextColor(itemView.getContext().getResources().getColor(R.color.banned_red, itemView.getContext().getTheme()));
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

    // --- LoadingViewHolder (The placeholder view for the loading spinner) ---
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        // A simple layout with a ProgressBar is expected in R.layout.item_loading
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // --- 4. Define the DiffUtil.ItemCallback to handle null/loading items ---
    private static final DiffUtil.ItemCallback<CardItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    // One is null (loading) but the other is not: NOT the same.
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem; // Only true if BOTH are null (both loading)
                    }
                    // Both are valid CardItems: check by name/ID
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    // Handle the null (loading) case
                    if (oldItem == null || newItem == null) {
                        return oldItem == newItem;
                    }
                    // Both are valid CardItems: use the CardItem's equals method
                    return oldItem.equals(newItem);
                }
            };
}