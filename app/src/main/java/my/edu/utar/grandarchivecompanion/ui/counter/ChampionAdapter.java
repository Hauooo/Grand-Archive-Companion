package my.edu.utar.grandarchivecompanion.ui.counter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import my.edu.utar.grandarchivecompanion.R;

public class ChampionAdapter extends RecyclerView.Adapter<ChampionAdapter.ChampionViewHolder> {

    // An interface to handle clicks
    public interface OnChampionClickListener {
        void onChampionClick(int position);
    }

    private String[] championNames;
    private int[] championImages;
    private OnChampionClickListener listener;

    public ChampionAdapter(String[] names, int[] images, OnChampionClickListener listener) {
        this.championNames = names;
        this.championImages = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChampionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // MODIFIED: Inflate our new carousel item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_item_champion, parent, false);
        return new ChampionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChampionViewHolder holder, int position) {
        holder.championImageView.setImageResource(championImages[position]);

        // Set the click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChampionClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return championNames.length;
    }

    static class ChampionViewHolder extends RecyclerView.ViewHolder {
        // MODIFIED: Reference the ImageView from our new layout
        ImageView championImageView;

        public ChampionViewHolder(@NonNull View itemView) {
            super(itemView);
            championImageView = itemView.findViewById(R.id.champion_image_view);
        }
    }
}