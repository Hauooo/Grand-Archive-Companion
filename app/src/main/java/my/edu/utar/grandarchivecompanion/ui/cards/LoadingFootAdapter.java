package my.edu.utar.grandarchivecompanion.ui.cards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import my.edu.utar.grandarchivecompanion.R;

public class LoadingFootAdapter extends RecyclerView.Adapter<LoadingFootAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_footer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Trigger the load when the view is bound (handles recycling correctly)
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return 1; // Only one loading foot item
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView loadingGif;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadingGif = itemView.findViewById(R.id.loading);
        }

        void bind() {
            // Load the GIF here to ensure it plays even if the ViewHolder was recycled
            Glide.with(itemView.getContext())
                    .asGif()
                    .load(R.drawable.loading)
                    .into(loadingGif);
        }
    }
}