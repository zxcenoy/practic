package com.example.practica;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends AppCompatActivity {
    ImageButton btnProf;
    private RecyclerView loyaltyRecyclerView;
    private RecyclerView coffeeRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_screen);
        btnProf = findViewById(R.id.profileBtn);
        findViewById(R.id.profileBtn).setOnClickListener(v -> gotoProfile());

        loyaltyRecyclerView = findViewById(R.id.loyaltyRecyclerView);
        LoyaltyAdapter loyaltyAdapter = new LoyaltyAdapter(getLoyaltyCups());
        loyaltyRecyclerView.setAdapter(loyaltyAdapter);
        loyaltyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        coffeeRecyclerView = findViewById(R.id.coffeeRecyclerView);
        CoffeeAdapter coffeeAdapter = new CoffeeAdapter(getCoffeeList());
        coffeeRecyclerView.setAdapter(coffeeAdapter);
        coffeeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
    private List<LoyaltyCup> getLoyaltyCups() {
        List<LoyaltyCup> cups = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cups.add(new LoyaltyCup(i < 4));
        }
        return cups;
    }
    private List<CoffeeItem> getCoffeeList() {
        List<CoffeeItem> coffeeItems = new ArrayList<>();
        coffeeItems.add(new CoffeeItem("Americano", R.drawable.americano_menu_photo));
        coffeeItems.add(new CoffeeItem("Cappuccino", R.drawable.cappuccino_menu_photo));
        coffeeItems.add(new CoffeeItem("Mocha", R.drawable.mocha_menu_photo));
        coffeeItems.add(new CoffeeItem("Flat White", R.drawable.flat_menu_photo));
        return coffeeItems;
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(MainScreen.this, Rewards.class));
            return true;
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(MainScreen.this, Orders.class));
            return true;
        }

        return false;
    };



    private void gotoProfile(){

            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);

    }

    public void BasketClick(View view){
        startActivity(new Intent(getApplicationContext(), MainScreen.class)); //TODO ПОМЕНЯЙ КОГДА СОЗДАШЬ КОРЗИНУ ЭКРАН
    }
}
