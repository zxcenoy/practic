package com.example.practica.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Classes.LoyaltyCup;
import com.example.practica.R;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyAdapter extends RecyclerView.Adapter<LoyaltyAdapter.LoyaltyViewHolder> {
    private List<LoyaltyCup> cups;

    public LoyaltyAdapter(List<LoyaltyCup> cups) {
        this.cups = cups;
    }
    public void updateCups(List<LoyaltyCup> newCups) {
        this.cups = new ArrayList<>(newCups);
        notifyDataSetChanged();
        Log.d("LoyaltyAdapter", "Updated all cups, count: " + newCups.size());
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
        if (position >= cups.size()){
            Log.e("LoyaltyAdapter", "Invalid position: " + position);

            return;
        }


        LoyaltyCup cup = cups.get(position);
        Log.d("LoyaltyAdapter", "Binding cup #" + position +
                " - earned: " + cup.isEarned() +
                " - hash: " + cup.hashCode());

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
                    cup.isEarned() ?
                            R.drawable.coffee_cup_active :
                            R.drawable.coffee_cup_inactive
            );
        }
    }
}
