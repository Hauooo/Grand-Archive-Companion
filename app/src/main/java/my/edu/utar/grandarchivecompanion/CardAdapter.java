package my.edu.utar.grandarchivecompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private final List<CardItem> cardList = new ArrayList<>();

    public CardAdapter(Context context) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_row, parent, false);
            return new CardViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardViewHolder) {
            CardItem card = cardList.get(position);
            ((CardViewHolder) holder).bind(card);
        }
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return cardList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setCards(List<CardItem> newCards) {
        CardDiffCallback diffCallback = new CardDiffCallback(this.cardList, newCards);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.cardList.clear();
        this.cardList.addAll(newCards);
        diffResult.dispatchUpdatesTo(this);
    }

    public void showLoadingFooter() {
        if (cardList.isEmpty() || cardList.get(cardList.size() - 1) != null) {
            cardList.add(null);
            notifyItemInserted(cardList.size() - 1);
        }
    }

    public void hideLoadingFooter() {
        if (!cardList.isEmpty() && cardList.get(cardList.size() - 1) == null) {
            int lastPosition = cardList.size() - 1;
            cardList.remove(lastPosition);
            notifyItemRemoved(lastPosition);
        }
    }

    public void addCards(List<CardItem> newCards) {
        int insertPosition = this.cardList.size();
        this.cardList.addAll(newCards);
        notifyItemRangeInserted(insertPosition, newCards.size());
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardName, cardType, cardText;
        ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardType = itemView.findViewById(R.id.card_type);
            cardText = itemView.findViewById(R.id.card_text);
            cardImage = itemView.findViewById(R.id.card_image);
        }

        void bind(CardItem card) {
            cardName.setText(card.getName());
            cardType.setText(card.getType());
            cardText.setText(card.getText());
            if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                Picasso.get().load(card.getImageUrl()).into(cardImage);
            } else {
                cardImage.setImageResource(android.R.color.darker_gray); // Placeholder
            }
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

class CardDiffCallback extends DiffUtil.Callback {
    private final List<CardItem> oldList;
    private final List<CardItem> newList;

    public CardDiffCallback(List<CardItem> oldList, List<CardItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override public int getOldListSize() { return oldList.size(); }
    @Override public int getNewListSize() { return newList.size(); }

    @Override
    public boolean areItemsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).getName().equals(newList.get(newPos).getName());
    }

    @Override
    public boolean areContentsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).equals(newList.get(newPos));
    }
}