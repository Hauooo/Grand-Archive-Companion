package my.edu.utar.grandarchivecompanion;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

// Note the change in generic types to handle multiple view types correctly
public class CardAdapter extends ListAdapter<CardItem, RecyclerView.ViewHolder> {

    // 1. Add the interface for click handling
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CardItem card);
    }

    public CardAdapter(@NonNull OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // You can re-add these constants if you implement the loading footer
    // private static final int VIEW_TYPE_ITEM = 0;
    // private static final int VIEW_TYPE_LOADING = 1;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // For now, we only create the main item view holder.
        // Logic for a loading view holder would be added here if needed.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_row, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Use getItem() from ListAdapter to get the object
        CardItem card = getItem(position);
        if (card != null && holder instanceof CardViewHolder) {
            ((CardViewHolder) holder).bind(card, listener);
        }
    }

    // You no longer need to override getItemCount(). ListAdapter does it for you.
    // You no longer need setCards(), addCards(), etc. You will use submitList() in your Fragment.

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardName, cardType, cardText;
        ImageView cardImage;
        private final android.content.res.ColorStateList defaultTextColor;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardType = itemView.findViewById(R.id.card_type);
            cardText = itemView.findViewById(R.id.card_text);
            cardImage = itemView.findViewById(R.id.card_image);
            defaultTextColor = cardName.getHintTextColors();
        }

        void bind(final CardItem card, final OnItemClickListener listener) {
            cardName.setText(card.getName());
            cardType.setText(card.getType());
            cardText.setText(card.getText());
            if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                Picasso.get().load(card.getImageUrl()).into(cardImage);
            } else {
                cardImage.setImageResource(android.R.color.darker_gray);
            }

            if (card.isBanned()){
                cardName.setTextColor(itemView.getContext().getResources().getColor(R.color.banned_red));
            }else{
                cardName.setTextColor(defaultTextColor);
            }

            // The click logic now calls the decoupled interface
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(card);
                }
            });

        }
    }

    // LoadingViewHolder can remain if you re-implement the loading footer logic
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // 2. The DiffUtil.ItemCallback is a static constant now
    private static final DiffUtil.ItemCallback<CardItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    // Should be a unique ID, but name is a fallback
                    return oldItem.getName().equals(newItem.getName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CardItem oldItem, @NonNull CardItem newItem) {
                    return oldItem.equals(newItem);
                }
            };
}