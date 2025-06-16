// CartActivity.java
package com.example.practica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {
    private CartAdapter adapter;
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(CartManager.getInstance().getItems(), this);
        rvCartItems.setAdapter(adapter);

        updateTotal();
    }

    private void updateTotal() {
        double total = CartManager.getInstance().getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        tvTotalPrice.setText(String.format("$%.2f", total));
    }

    @Override
    public void onItemRemoved(int position) {
        CartManager.getInstance().removeItem(position);
        adapter.notifyItemRemoved(position);
        updateTotal();
    }

    @Override
    public void onQuantityChanged() {
        updateTotal();
    }
}