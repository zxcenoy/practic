package com.example.practica.Adapters;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.practica.Items.CoffeeItem;
import com.example.practica.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.CoffeeViewHolder> {
    private List<CoffeeItem> originalItems;
    private List<CoffeeItem> filteredItems;
    private OnCoffeeClickListener listener;

    public interface OnCoffeeClickListener {
        void onCoffeeClick(CoffeeItem item);
    }

    public CoffeeAdapter(List<CoffeeItem> coffeeItems, OnCoffeeClickListener listener) {
        this.originalItems = new ArrayList<>(coffeeItems);
        this.filteredItems = new ArrayList<>(coffeeItems);
        this.listener = listener;
    }

    public void filter(String category, double maxPrice) {
        filteredItems.clear();

        Log.d("CoffeeFilter", "Filtering by: " + category);

        for (CoffeeItem item : originalItems) {
            String itemCategory = item.getCategory() != null ? item.getCategory().toLowerCase() : "unknown";

            Log.d("CoffeeFilter", "Item: " + item.getName() +
                    " | Category: " + itemCategory +
                    " | Price: " + item.getPrice());

            boolean matchesCategory =
                    "all".equals(category) ||
                            ("black".equals(category) && itemCategory.contains("черный")) ||
                            ("milk".equals(category) && itemCategory.contains("молочный"));

            boolean matchesPrice = item.getPrice() <= maxPrice;

            if (matchesCategory && matchesPrice) {
                filteredItems.add(item);
                Log.d("CoffeeFilter", "MATCHED: " + item.getName());
            }
        }

        Log.d("CoffeeFilter", "Results: " + filteredItems.size() + " items");
        notifyDataSetChanged();
    }

    public void resetFilters() {
        filteredItems.clear();
        filteredItems.addAll(originalItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoffeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coffee, parent, false);
        return new CoffeeViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CoffeeViewHolder holder, int position) {
        CoffeeItem item = filteredItems.get(position);
        holder.bind(item, listener);
    }

    static class CoffeeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView priceView;

        public CoffeeViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.coffeeImage);
            nameView = itemView.findViewById(R.id.coffeeName);
            priceView = itemView.findViewById(R.id.coffeePrice);
        }

        public void bind(CoffeeItem item, OnCoffeeClickListener listener) {
            nameView.setText(item.getName());
            priceView.setText(String.format("$%.2f", item.getPrice()));

            Picasso.get()
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.coffee_cup_details)
                    .error(R.drawable.crestik_icon)
                    .into(imageView);

            itemView.setOnClickListener(v -> listener.onCoffeeClick(item));
        }
    }
}