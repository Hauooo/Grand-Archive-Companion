package my.edu.utar.grandarchivecompanion;

import android.content.Context;
import android.content.Intent;
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

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder>{

    private final Context context;
    private List<CardItem> cardList = new ArrayList<>();

    public CardAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem card = cardList.get(position);

        holder.cardName.setText(card.getName());
        Picasso.get()
                .load(card.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.cardImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CardDetailActivity.class);
            intent.putExtra(CardDetailActivity.EXTRA_IMAGE_URL, card.getImageUrl());
            intent.putExtra(CardDetailActivity.EXTRA_NAME, card.getName());
            intent.putExtra(CardDetailActivity.EXTRA_TYPE, card.getType());
            intent.putExtra(CardDetailActivity.EXTRA_TEXT, card.getText());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // 🔎 Implement filtering
   public void updateList(List<CardItem> newList) {
        CardDiffcallback diffCallback = new CardDiffcallback(this.cardList, newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.cardList.clear();
        this.cardList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardName;
        ImageView cardImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardImage = itemView.findViewById(R.id.card_image);
        }
    }
}

class CardDiffcallback extends DiffUtil.Callback {
    private final List<CardItem> oldList;
    private final List<CardItem> newList;

    public CardDiffcallback(List<CardItem> oldList, List<CardItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}