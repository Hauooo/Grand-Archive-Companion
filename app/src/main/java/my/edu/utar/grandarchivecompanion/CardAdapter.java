package my.edu.utar.grandarchivecompanion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> implements Filterable {

    private Context context;
    private List<CardItem> cardList;
    private List<CardItem> cardListFull; // backup copy for filtering

    public CardAdapter(Context context, List<CardItem> cardList) {
        this.context = context;
        this.cardList = cardList;
        this.cardListFull = new ArrayList<>(cardList);
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
            intent.putExtra("imageUrl", card.getImageUrl());
            intent.putExtra("name", card.getName());
            intent.putExtra("type", card.getType());
            intent.putExtra("text", card.getText());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // 🔎 Implement filtering
    @Override
    public Filter getFilter() {
        return cardFilter;
    }

    private final Filter cardFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<CardItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(cardListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (CardItem item : cardListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            cardList.clear();
            cardList.addAll((List<CardItem>) results.values);
            notifyDataSetChanged();
        }
    };

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
