package my.edu.utar.grandarchivecompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChampionAdapter extends RecyclerView.Adapter<ChampionAdapter.ViewHolder> {

    public interface OnChampionClickListener {
        void onChampionClick(int position);
    }

    private String[] champions;
    private int[] championImages;
    private OnChampionClickListener listener;

    public ChampionAdapter(String[] champions, int[] championImages, OnChampionClickListener listener) {
        this.champions = champions;
        this.championImages = championImages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChampionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_champion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChampionAdapter.ViewHolder holder, int position) {
        holder.championName.setText(champions[position]);
        holder.championIcon.setImageResource(championImages[position]);
        holder.itemView.setOnClickListener(v -> listener.onChampionClick(position));
    }

    @Override
    public int getItemCount() {
        return champions.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView championIcon;
        TextView championName;

        public ViewHolder(View itemView) {
            super(itemView);
            championIcon = itemView.findViewById(R.id.champion_icon);
            championName = itemView.findViewById(R.id.champion_name);
        }
    }
}
