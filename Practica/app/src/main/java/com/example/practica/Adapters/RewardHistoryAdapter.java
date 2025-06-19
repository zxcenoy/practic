package com.example.practica.Adapters;

import android.util.Log;
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
    public void updateData(List<RewardItem> newItems) {
        this.rewardItems.clear();
        this.rewardItems.addAll(newItems);
        Log.d("Adapter", "Updating with " + newItems.size() + " items");
        notifyDataSetChanged();
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
        String displayName = item.getDrinkName().isEmpty() ? "Заказ" : item.getDrinkName();

        holder.drinkName.setText(displayName);
        holder.points.setText("+ " + item.getPoints() + " Pts");
        holder.date.setText(item.getFormattedDate());

    }

    @Override
    public int getItemCount() {
        return rewardItems.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView drinkName, points, date;
        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkName = itemView.findViewById(R.id.drinkName);
            points = itemView.findViewById(R.id.points);
            date = itemView.findViewById(R.id.date);
        }
    }
}