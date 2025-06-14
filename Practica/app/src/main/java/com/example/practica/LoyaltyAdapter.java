package com.example.practica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoyaltyAdapter extends RecyclerView.Adapter<LoyaltyAdapter.LoyaltyViewHolder> {
    private List<LoyaltyCup> cups;

    public LoyaltyAdapter(List<LoyaltyCup> cups) {
        this.cups = cups;
    }

    @NonNull
    @Override
    public LoyaltyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loyalty_cup, parent, false);
        return new LoyaltyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoyaltyViewHolder holder, int position) {
        LoyaltyCup cup = cups.get(position);
        holder.bind(cup);
    }

    @Override
    public int getItemCount() {
        return cups.size();
    }

    static class LoyaltyViewHolder extends RecyclerView.ViewHolder {
        ImageView cupImage;

        public LoyaltyViewHolder(@NonNull View itemView) {
            super(itemView);
            cupImage = itemView.findViewById(R.id.cupImage);
        }

        public void bind(LoyaltyCup cup) {
            cupImage.setImageResource(
                    cup.isActive() ?
                            R.drawable.coffee_cup_active :
                            R.drawable.coffee_cup_inactive
            );
        }
    }
}
