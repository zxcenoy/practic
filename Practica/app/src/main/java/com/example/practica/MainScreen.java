package com.example.practica;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainScreen extends AppCompatActivity {
    ImageButton btnProf;
    private TextView greetingText;
    private TextView filterGreetingText;
    private TextView filterUserName;
    private DrawerLayout drawerLayout;
    private LinearLayout filterMenu;
    private RecyclerView loyaltyRecyclerView;
    private RecyclerView coffeeRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_screen);

        btnProf = findViewById(R.id.profileBtn);
        greetingText = findViewById(R.id.greetingText);
        filterGreetingText = findViewById(R.id.filterGreeting);
        filterUserName = findViewById(R.id.filterUserName);

        loadUserName();

        btnProf.setOnClickListener(v -> gotoProfile());
        findViewById(R.id.basketBtn).setOnClickListener(this::BasketClick);

        loyaltyRecyclerView = findViewById(R.id.loyaltyRecyclerView);
        LoyaltyAdapter loyaltyAdapter = new LoyaltyAdapter(getLoyaltyCups());
        loyaltyRecyclerView.setAdapter(loyaltyAdapter);
        loyaltyRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        coffeeRecyclerView = findViewById(R.id.coffeeRecyclerView);
        CoffeeAdapter coffeeAdapter = new CoffeeAdapter(getCoffeeList());
        coffeeRecyclerView.setAdapter(coffeeAdapter);
        coffeeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_home);

        initFilters();
    }

    private void initFilters() {
        drawerLayout = findViewById(R.id.drawerLayout);
        filterMenu = findViewById(R.id.filterMenu);

        ImageView filterIcon = findViewById(R.id.filter);
        filterIcon.setOnClickListener(v -> toggleFilterMenu());

        Button applyFilters = findViewById(R.id.applyFilters);
        applyFilters.setOnClickListener(v -> {
            applyFilters();
            drawerLayout.closeDrawer(filterMenu);
        });

        TextView resetFilters = findViewById(R.id.resetFilters);
        resetFilters.setOnClickListener(v -> resetFilters());

        ImageView closeFilter = findViewById(R.id.closeFilter);
        closeFilter.setOnClickListener(v -> drawerLayout.closeDrawer(filterMenu));
    }

    private void toggleFilterMenu() {
        if (drawerLayout.isDrawerOpen(filterMenu)) {
            drawerLayout.closeDrawer(filterMenu);
        } else {
            drawerLayout.openDrawer(filterMenu);
        }
    }

    private void applyFilters() {
        RadioGroup categoryGroup = findViewById(R.id.categoryGroup);
        SeekBar priceSeekBar = findViewById(R.id.priceSeekBar);

        String category = "all";
        int selectedId = categoryGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.blackCoffee) {
            category = "black";
        } else if (selectedId == R.id.milkCoffee) {
            category = "milk";
        }

        int maxPrice = priceSeekBar.getProgress() + 1;

        CoffeeAdapter adapter = (CoffeeAdapter) coffeeRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.filter(category, maxPrice);
        }
    }

    private void resetFilters() {
        RadioGroup categoryGroup = findViewById(R.id.categoryGroup);
        SeekBar priceSeekBar = findViewById(R.id.priceSeekBar);

        categoryGroup.check(R.id.allCoffee);
        priceSeekBar.setProgress(9);

        CoffeeAdapter adapter = (CoffeeAdapter) coffeeRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.resetFilters();
        }
    }

    private void loadUserName() {
        SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String timeGreeting;
        if (timeOfDay < 12) {
            timeGreeting = "Good morning";
        } else if (timeOfDay < 16) {
            timeGreeting = "Good afternoon";
        } else {
            timeGreeting = "Good evening";
        }

        String greeting = timeGreeting + ", " + userName;
        greetingText.setText(greeting);

        filterGreetingText.setText(timeGreeting);
        filterUserName.setText(userName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserName();
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
        coffeeItems.add(new CoffeeItem("Americano", R.drawable.americano_menu_photo, "black", 3));
        coffeeItems.add(new CoffeeItem("Cappuccino", R.drawable.cappuccino_menu_photo, "milk", 4));
        coffeeItems.add(new CoffeeItem("Mocha", R.drawable.mocha_menu_photo, "milk", 5));
        coffeeItems.add(new CoffeeItem("Flat White", R.drawable.flat_menu_photo, "milk", 5));
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

    private void gotoProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    public void BasketClick(View view) {
        startActivity(new Intent(getApplicationContext(), MainScreen.class)); //TODO Замени на  экран корзины когда сделашеь
    }
}