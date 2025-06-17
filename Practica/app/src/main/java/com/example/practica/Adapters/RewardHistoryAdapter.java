package com.example.practica.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Items.RewardItem;
import com.example.practica.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RewardHistoryAdapter extends RecyclerView.Adapter<RewardHistoryAdapter.RewardViewHolder> {
    private List<RewardItem> rewardItems;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public RewardHistoryAdapter(List<RewardItem> rewardItems) {
        this.rewardItems = rewardItems;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward_history_down, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardItem item = rewardItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return rewardItems.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView drinkNameTextView;
        TextView pointsTextView;
        TextView dateTextView;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkNameTextView = itemView.findViewById(R.id.drinkName);
            pointsTextView = itemView.findViewById(R.id.points);
            dateTextView = itemView.findViewById(R.id.date);
        }

        public void bind(RewardItem item) {
            drinkNameTextView.setText(item.getDrinkName());
            pointsTextView.setText(String.format(Locale.getDefault(), "+ %d Pts", item.getPoints()));
            dateTextView.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(item.getDate()));
        }
    }
}