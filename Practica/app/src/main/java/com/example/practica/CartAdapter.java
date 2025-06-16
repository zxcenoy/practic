// CartAdapter.java
package com.example.practica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final List<CoffeeOrder> items;
    private final OnCartItemListener listener;

    public interface OnCartItemListener {
        void onItemRemoved(int position);
        void onQuantityChanged();
    }

    public CartAdapter(List<CoffeeOrder> items, OnCartItemListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvOptions, tvPrice, tvQuantity;
        private final AppCompatButton btnIncrease, btnDecrease;
        private final ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvOptions = itemView.findViewById(R.id.tvItemOptions);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }

        void bind(CoffeeOrder order, int position) {
            tvName.setText(order.getName());
            tvOptions.setText(order.getDescription());
            tvPrice.setText(String.format("$%.2f", order.getPrice() * order.getQuantity()));
            tvQuantity.setText(String.valueOf(order.getQuantity()));

            btnRemove.setOnClickListener(v -> listener.onItemRemoved(position));

            btnIncrease.setOnClickListener(v -> {
                order.setQuantity(order.getQuantity() + 1);
                updateQuantity(order);
                listener.onQuantityChanged();
            });

            btnDecrease.setOnClickListener(v -> {
                if (order.getQuantity() > 1) {
                    order.setQuantity(order.getQuantity() - 1);
                    updateQuantity(order);
                    listener.onQuantityChanged();
                }
            });

            updateButtonStyles();
        }

        private void updateQuantity(CoffeeOrder order) {
            tvQuantity.setText(String.valueOf(order.getQuantity()));
            tvPrice.setText(String.format("$%.2f", order.getPrice() * order.getQuantity()));
            updateButtonStyles();
        }

        private void updateButtonStyles() {
            int quantity = Integer.parseInt(tvQuantity.getText().toString());
            btnDecrease.setBackgroundResource(
                    quantity > 1 ? R.drawable.shot_btn_active : R.drawable.shot_btn_inactive
            );
        }
    }
}