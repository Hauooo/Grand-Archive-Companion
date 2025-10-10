package my.edu.utar.grandarchivecompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    // MODIFIED: The list now holds LogEntry objects
    private List<LogEntry> logEntries = new ArrayList<>();
    private Context context;

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Store context for getting colors
        View view = LayoutInflater.from(context).inflate(R.layout.log_item_row, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        // Get the LogEntry for the current row
        LogEntry currentEntry = logEntries.get(position);

        // Set the text
        holder.logTextView.setText(currentEntry.getLogText());

        // --- NEW: Set the color based on the player index ---
        int colorResId;
        if (currentEntry.getPlayerIndex() == 0) {
            // It's Player 1, use a color (e.g., a cool blue)
            colorResId = R.color.player_a_color; // Define these colors in res/values/colors.xml
        } else {
            // It's Player 2, use a different color (e.g., a warm orange)
            colorResId = R.color.player_b_color;
        }

        holder.logTextView.setTextColor(ContextCompat.getColor(context, colorResId));
    }

    @Override
    public int getItemCount() {
        return logEntries.size();
    }

    // MODIFIED: This method now accepts a List of LogEntry objects
    public void updateLogs(List<LogEntry> newLogs) {
        this.logEntries.clear();
        this.logEntries.addAll(newLogs);
        notifyDataSetChanged();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView logTextView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTextView = itemView.findViewById(R.id.log_entry_text);
        }
    }
}