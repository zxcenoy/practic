package com.example.practica.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Adapters.CoffeeAdapter;
import com.example.practica.Items.CoffeeItem;
import com.example.practica.Adapters.LoyaltyAdapter;
import com.example.practica.Classes.LoyaltyCup;
import com.example.practica.Managers.AuthManager;
import com.example.practica.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainScreen extends AppCompatActivity {
    private CoffeeAdapter adapter;
    ImageButton btnProf;
    private TextView greetingText;
    private TextView filterGreetingText;
    private TextView filterUserName;
    private DrawerLayout drawerLayout;
    private LinearLayout filterMenu;
    private RecyclerView loyaltyRecyclerView;
    private RecyclerView coffeeRecyclerView;
    AuthManager authManager;
    private TextView loyaltyProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        authManager = new AuthManager(getApplicationContext());
        if (!authManager.isTokenValid(this)) {
            redirectToSignIn();
            return;
        }
        setContentView(R.layout.main_screen);

        loyaltyProgressText = findViewById(R.id.loyaltyMainScreen);


        coffeeRecyclerView = findViewById(R.id.coffeeRecyclerView);
        coffeeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CoffeeAdapter(new ArrayList<>(), this::openCoffeeDetails);
        coffeeRecyclerView.setAdapter(adapter);

        ImageButton btnCart = findViewById(R.id.basketBtn);
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        btnProf = findViewById(R.id.profileBtn);
        greetingText = findViewById(R.id.greetingText);
        filterGreetingText = findViewById(R.id.filterGreeting);
        filterUserName = findViewById(R.id.filterUserName);

        loadUserName();

        btnProf.setOnClickListener(v -> gotoProfile());

        loyaltyRecyclerView = findViewById(R.id.loyaltyRecyclerView1);
        LoyaltyAdapter loyaltyAdapter = new LoyaltyAdapter(getLoyaltyCups());
        loyaltyRecyclerView.setAdapter(loyaltyAdapter);
        loyaltyRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));



        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_home);
        loadProductsFromSupabase();

        initFilters();
        loadUserData();

    }
    private void loadUserData() {
        AuthManager authManager = new AuthManager(this);
        String userId = authManager.getCurrentUserId();
        if (userId == null) return;

        authManager.updateLoyaltyStatus(userId, new AuthManager.LoyaltyCallback() {
            @Override
            public void onSuccess(int completedOrders, int cupsEarned) {
                runOnUiThread(() -> {
                    loyaltyProgressText.setText(completedOrders + " / 8");

                    LoyaltyAdapter adapter = (LoyaltyAdapter) loyaltyRecyclerView.getAdapter();
                    if (adapter != null) {
                        List<LoyaltyCup> cups = new ArrayList<>();
                        for (int i = 0; i < 8; i++) {
                            cups.add(new LoyaltyCup(i < cupsEarned));
                        }
                        adapter.updateCups(cups);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("MainScreen", "Loyalty error: " + error);
            }
        });
    }


    private void openCoffeeDetails(CoffeeItem coffee) {
        if (coffee.getProductId() < 0) {
            return;
        }

        Intent intent = new Intent(this, CoffeeDetailActivity.class);
        intent.putExtra("coffee_name", coffee.getName());
        intent.putExtra("coffee_price", coffee.getPrice());
        intent.putExtra("coffee_image_url", coffee.getImageUrl());
        intent.putExtra("product_id", coffee.getProductId());
        startActivity(intent);
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

        double maxPrice = priceSeekBar.getProgress() + 1;

        Log.d("MainScreen", "Applying filters - Category: " + category +
                ", MaxPrice: " + maxPrice);

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
        if (!authManager.isTokenValid(this)) {
            redirectToSignIn();
            return;
        }
        loadUserName();
    }
    private void redirectToSignIn() {
        authManager.clearAuthData();

        Intent intent = new Intent(this, SignIn.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, getString(R.string.SessionExpired), Toast.LENGTH_SHORT).show();
    }


    private List<LoyaltyCup> getLoyaltyCups() {
        List<LoyaltyCup> cups = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cups.add(new LoyaltyCup(i < 4));
        }
        return cups;
    }
    private void loadProductsFromSupabase() {
        if (!authManager.isTokenValid(this)) {
            redirectToSignIn();
            return;
        }
        authManager.getProducts(new AuthManager.ProductCallback() {
            @Override
            public void onSuccess(List<CoffeeItem> products) {
                runOnUiThread(() -> {
                    Log.d("DataLoad", "Loaded " + products.size() + " items");
                    if (products.isEmpty()) {
                        loadLocalProducts();
                    } else {
                        adapter = new CoffeeAdapter(products, MainScreen.this::openCoffeeDetails);
                        coffeeRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (!authManager.isTokenValid(MainScreen.this)) {
                        redirectToSignIn();
                    } else {
                        Log.e("MainScreen", "Ошибка загрузки: " + error);
                        loadLocalProducts();
                    }
                });
            }
        });
    }

    private void loadLocalProducts() {
        List<CoffeeItem> localItems = new ArrayList<>();
        localItems.add(new CoffeeItem("Americano", R.drawable.americano_menu_photo, "black", 3.0));
        localItems.add(new CoffeeItem("Cappuccino", R.drawable.cappuccino_menu_photo, "milk", 4.0));
        localItems.add(new CoffeeItem("Mocha", R.drawable.mocha_menu_photo, "milk", 5.0));
        localItems.add(new CoffeeItem("Flat White", R.drawable.flat_menu_photo, "milk", 5.0));

        adapter = new CoffeeAdapter(localItems, MainScreen.this::openCoffeeDetails);
        coffeeRecyclerView.setAdapter(adapter);
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(MainScreen.this, Rewards.class));
            return true;
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(MainScreen.this, OrdersActivity.class));
            return true;
        }
        return false;
    };

    private void gotoProfile() {
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    public void BasketClick(View view) {
        startActivity(new Intent(getApplicationContext(), CartActivity.class));
    }
}