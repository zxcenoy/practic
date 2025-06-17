package com.example.practica.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.practica.Classes.CoffeeOrder;
import com.example.practica.Managers.CartManager;
import com.example.practica.R;

public class CoffeeDetailActivity extends AppCompatActivity {
    private int quantity = 1;
    private double basePrice = 3.00;
    private String selectedShot = "Single";
    private String selectedSelect = "Cup";
    private String selectedSize = "Small";
    private String selectedIce = "No Ice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_detail);

        Intent intent = getIntent();
        String coffeeName = intent.getStringExtra("coffee_name");
        basePrice = intent.getDoubleExtra("coffee_price", 3.00);
        int imageRes = intent.getIntExtra("coffee_image", R.drawable.americano_menu_photo);

        TextView tvCoffeeName = findViewById(R.id.coffeeName);
        tvCoffeeName.setText(coffeeName);

        ImageView ivCoffee = findViewById(R.id.coffeeImage);
        ivCoffee.setImageResource(imageRes);

        initNavigation();
        initQuantityControls();
        initShotSelection();
        initSelectOptions();
        initSizeSelection();
        initIceSelection();
        initCheckoutButton();

        updateTotalPrice();
    }

    private void initNavigation() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageButton btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private void initQuantityControls() {
        AppCompatButton btnDecrease = findViewById(R.id.btnDecrease);
        AppCompatButton btnIncrease = findViewById(R.id.btnIncrease);
        TextView tvQuantity = findViewById(R.id.tvQuantity);

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });
    }

    private void initShotSelection() {
        AppCompatButton btnSingle = findViewById(R.id.btnSingle);
        AppCompatButton btnDouble = findViewById(R.id.btnDouble);

        btnSingle.setSelected(true);
        updateButtonAppearance(btnSingle, true);
        updateButtonAppearance(btnDouble, false);

        btnSingle.setOnClickListener(v -> {
            if (!btnSingle.isSelected()) {
                selectedShot = "Single";
                btnSingle.setSelected(true);
                btnDouble.setSelected(false);
                updateButtonAppearance(btnSingle, true);
                updateButtonAppearance(btnDouble, false);
                updateTotalPrice();
            }
        });

        btnDouble.setOnClickListener(v -> {
            if (!btnDouble.isSelected()) {
                selectedShot = "Double";
                btnDouble.setSelected(true);
                btnSingle.setSelected(false);
                updateButtonAppearance(btnDouble, true);
                updateButtonAppearance(btnSingle, false);
                updateTotalPrice();
            }
        });
    }

    private void updateButtonAppearance(AppCompatButton button, boolean isSelected) {
        button.setBackgroundResource(isSelected ? R.drawable.shot_btn_active : R.drawable.shot_btn_inactive);
        button.setTextColor(ContextCompat.getColor(this,
                isSelected ? android.R.color.white : android.R.color.black));
    }

    private void initSelectOptions() {
        ImageButton btnCup = findViewById(R.id.btnSelect1);
        ImageButton btnGlass = findViewById(R.id.btnSelect2);

        btnCup.setImageResource(R.drawable.coffee_cup_details);
        btnGlass.setImageResource(R.drawable.coffee_stakan_details);

        btnCup.setOnClickListener(v -> {
            selectedSelect = "Cup";
            btnCup.setImageResource(R.drawable.coffee_cup_details);
            btnGlass.setImageResource(R.drawable.coffee_stakan_details);
        });

        btnGlass.setOnClickListener(v -> {
            selectedSelect = "Glass";
            btnGlass.setImageResource(R.drawable.coffee_stakan_details);
            btnCup.setImageResource(R.drawable.coffee_cup_details);
        });
    }

    private void initSizeSelection() {
        ImageButton btnSmall = findViewById(R.id.btnSmall);
        ImageButton btnMedium = findViewById(R.id.btnMedium);
        ImageButton btnLarge = findViewById(R.id.btnLarge);

        btnSmall.setImageResource(R.drawable.coffee_size_small_inactive);
        btnMedium.setImageResource(R.drawable.coffee_size_medium_inactive);
        btnLarge.setImageResource(R.drawable.coffee_size_large_inactive);

        View.OnClickListener sizeListener = v -> {
            int id = v.getId();
            selectedSize = id == R.id.btnSmall ? "Small" :
                    id == R.id.btnMedium ? "Medium" : "Large";

            btnSmall.setImageResource(id == R.id.btnSmall ?
                    R.drawable.coffee_size_small_active : R.drawable.coffee_size_small_inactive);
            btnMedium.setImageResource(id == R.id.btnMedium ?
                    R.drawable.coffee_size_medium_active : R.drawable.coffee_size_medium_inactive);
            btnLarge.setImageResource(id == R.id.btnLarge ?
                    R.drawable.coffee_size_large_active : R.drawable.coffee_size_large_inactive);

            updateTotalPrice();
        };

        btnSmall.setOnClickListener(sizeListener);
        btnMedium.setOnClickListener(sizeListener);
        btnLarge.setOnClickListener(sizeListener);
    }

    private void initIceSelection() {
        ImageButton btnNoIce = findViewById(R.id.btnNoIce);
        ImageButton btnLightIce = findViewById(R.id.btnLightIce);
        ImageButton btnFullIce = findViewById(R.id.btnFullIce);

        btnNoIce.setImageResource(R.drawable.ice_small_inactive);
        btnLightIce.setImageResource(R.drawable.ice_medium_inactive);
        btnFullIce.setImageResource(R.drawable.ice_large_active);

        View.OnClickListener iceListener = v -> {
            int id = v.getId();
            selectedIce = id == R.id.btnNoIce ? "No Ice" :
                    id == R.id.btnLightIce ? "Light Ice" : "Full Ice";

            btnNoIce.setImageResource(id == R.id.btnNoIce ?
                    R.drawable.ice_small_active : R.drawable.ice_small_inactive);
            btnLightIce.setImageResource(id == R.id.btnLightIce ?
                    R.drawable.ice_medium_active : R.drawable.ice_medium_inactive);
            btnFullIce.setImageResource(id == R.id.btnFullIce ?
                    R.drawable.ice_large_active : R.drawable.ice_large_inactive);
        };

        btnNoIce.setOnClickListener(iceListener);
        btnLightIce.setOnClickListener(iceListener);
        btnFullIce.setOnClickListener(iceListener);
    }

    private void initCheckoutButton() {
        Button btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            CoffeeOrder order = createOrder();
            CartManager.getInstance().addToCart(order);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private CoffeeOrder createOrder() {
        double finalPrice = calculateCurrentPrice();
        String description = String.format("%s shot | %s | %s | %s",
                selectedShot, selectedIce, selectedSize, selectedSelect);

        return new CoffeeOrder(
                ((TextView) findViewById(R.id.coffeeName)).getText().toString(),
                finalPrice,
                description,
                quantity
        );
    }

    private double calculateCurrentPrice() {
        double multiplier = 1.0;

        switch (selectedSize) {
            case "Medium": multiplier = 1.2; break;
            case "Large": multiplier = 1.5; break;
        }

        if ("Double".equals(selectedShot)) {
            multiplier *= 1.5;
        }

        return basePrice * multiplier * quantity;
    }

    private void updateTotalPrice() {
        double totalPrice = calculateCurrentPrice();
        TextView tvTotal = findViewById(R.id.totalAmount);
        tvTotal.setText(String.format("Total Amount: $%.2f", totalPrice));
    }
}