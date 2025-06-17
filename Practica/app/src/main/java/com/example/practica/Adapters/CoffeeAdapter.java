package com.example.practica.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Items.CoffeeItem;
import com.example.practica.R;

import java.util.ArrayList;
import java.util.List;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder> {
    private List<CoffeeItem> coffeeItems;
    private List<CoffeeItem> coffeeItemsFull;
    private OnCoffeeClickListener listener;

    public interface OnCoffeeClickListener {
        void onCoffeeClick(CoffeeItem item);
    }

    public CoffeeAdapter(List<CoffeeItem> coffeeItems) {
        this.coffeeItems = coffeeItems;
        this.coffeeItemsFull = new ArrayList<>(coffeeItems);
    }
    public CoffeeAdapter(List<CoffeeItem> coffeeItems, OnCoffeeClickListener listener) {
        this.coffeeItems = coffeeItems;
        this.coffeeItemsFull = new ArrayList<>(coffeeItems);
        this.listener = listener;
    }

    public void filter(String category, int maxPrice) {
        List<CoffeeItem> filteredList = new ArrayList<>();

        for (CoffeeItem item : coffeeItemsFull) {
            boolean matchesCategory = category.equals("all") ||
                    item.getCategory().equalsIgnoreCase(category);
            boolean matchesPrice = item.getPrice() <= maxPrice;

            if (matchesCategory && matchesPrice) {
                filteredList.add(item);
            }
        }

        coffeeItems = filteredList;
        notifyDataSetChanged();
    }

    public void resetFilters() {
        coffeeItems = new ArrayList<>(coffeeItemsFull);
        notifyDataSetChanged();
    }

    static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        ImageView coffeeImage;
        TextView coffeeName;
        TextView coffeePrice;

        public CoffeeViewHolder(@NonNull View itemView) {
            super(itemView);
            coffeeImage = itemView.findViewById(R.id.coffeeImage);
            coffeeName = itemView.findViewById(R.id.coffeeName);
            coffeePrice = itemView.findViewById(R.id.coffeePrice);
        }

        public void bind(CoffeeItem item) {
            coffeeImage.setImageResource(item.getImageResId());
            coffeeName.setText(item.getName());
            coffeePrice.setText(String.format("$%.2f", item.getPrice()));
        }
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
}