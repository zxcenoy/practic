package com.example.practica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder> {
    private List<CoffeeItem> coffeeItems;
    private OnCoffeeClickListener listener;

    public interface OnCoffeeClickListener {
        void onCoffeeClick(CoffeeItem item);
    }

    public CoffeeAdapter(List<CoffeeItem> coffeeItems) {
        this.coffeeItems = coffeeItems;
    }

    public void setOnCoffeeClickListener(OnCoffeeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffee, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        CoffeeItem item = coffeeItems.get(position);
        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCoffeeClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coffeeItems.size();
    }

    static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        ImageView coffeeImage;
        TextView coffeeName;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            coffeeImage = itemView.findViewById(R.id.coffeeImage);
            coffeeName = itemView.findViewById(R.id.coffeeName);
        }

        public void bind(CoffeeItem item) {
            coffeeImage.setImageResource(item.getImageResId());
            coffeeName.setText(item.getName());
        }
    }
}