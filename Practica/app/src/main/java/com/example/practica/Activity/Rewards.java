package com.example.practica.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Adapters.LoyaltyAdapter;
import com.example.practica.Adapters.RewardHistoryAdapter;
import com.example.practica.Classes.LoyaltyCup;
import com.example.practica.Managers.AuthManager;
import com.example.practica.R;
import com.example.practica.Items.RewardItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Rewards extends AppCompatActivity {
    private TextView myPointsText;
    private TextView loyaltyProgressText;
    private List<RewardItem> rewardHistory = new ArrayList<>();

    AuthManager authManager;
    LoyaltyAdapter loyaltyAdapter;
    RewardHistoryAdapter rewardHistoryAdapter;

    RecyclerView rewardHistoryRecyclerView;
    RecyclerView loyaltyRecyclerView;
    int[] cups ={0};
    List<LoyaltyCup> cup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rewards);

        cup = Arrays.asList(new LoyaltyCup(false),new LoyaltyCup(false),new LoyaltyCup(false),new LoyaltyCup(false),new LoyaltyCup(false),
        new LoyaltyCup(false),new LoyaltyCup(false),new LoyaltyCup(false));

        loyaltyProgressText = findViewById(R.id.loyaltyProgressText);
        myPointsText = findViewById(R.id.myPointsText);

        rewardHistoryRecyclerView = findViewById(R.id.rewardHistoryRecyclerView);
        rewardHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        authManager = new AuthManager(this);

        rewardHistoryAdapter = new RewardHistoryAdapter(rewardHistory);
        rewardHistoryRecyclerView.setAdapter(rewardHistoryAdapter);

        loyaltyRecyclerView = findViewById(R.id.loyaltyRecyclerView);
        /*loyaltyRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));*/
        loyaltyAdapter = new LoyaltyAdapter(cup);
        loyaltyRecyclerView.setAdapter(loyaltyAdapter);


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_rewards);

        String userId = authManager.getCurrentUserId();
        if (userId != null) {
            loadUserData(userId);
        }
        loadUserPoints();
    }

    private void loadUserPoints() {
        String userId = authManager.getCurrentUserId();
        if (userId == null) return;

        authManager.calculateAndUpdateUserPoints(userId, new AuthManager.PointsCallback() {
            @Override
            public void onSuccess(int points) {
                runOnUiThread(() -> {
                    myPointsText.setText(getString(R.string.MyPoints) + points);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("RewardsActivity", "Error loading points: " + error);
                });
            }
        });
    }

    private void loadUserData(String userId) {
        authManager.updateLoyaltyStatus(userId, new AuthManager.LoyaltyCallback() {
            @Override
            public void onSuccess(int completedOrders, int cupsEarned) {
                runOnUiThread(() -> {
                    Log.d("Rewards", "Updating UI with cups earned: " + cupsEarned);
                    loyaltyProgressText.setText(completedOrders + " / 8");

                });
            }
            @Override
            public void onError(String error) {
                Log.e("Rewards", "Loyalty error: " + error);
            }
        });

        authManager.getRewardHistory(userId, new AuthManager.RewardHistoryCallback() {
            @Override
            public void onSuccess(List<RewardItem> history) {
                runOnUiThread(() -> {
                    if (history != null) {
                        Log.d("Rewards", "Received history items: " + history.size());
                        rewardHistoryAdapter.updateData(history);
                        setupLoyaltyCups(history.size() / 5);

                    } else {
                        Log.d("Rewards", "History is null");
                    }
                });
            }
            @Override
            public void onError(String error) {
                Log.e("Rewards", "History error: " + error);
            }
        });
    }

    private void setupLoyaltyCups(int cupsEarned) {
        for (int i = 0; i < 8; i++) {
            cup.get(i).setEarned(true);
        }
        loyaltyAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();

        AuthManager authManager = new AuthManager(this);
        if (!authManager.isTokenValid(this)) {
            authManager.clearAuthData();

            Intent intent = new Intent(this, SignIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, getString(R.string.SessionExpired), Toast.LENGTH_SHORT).show();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(Rewards.this, MainScreen.class));
            finish();
            return true;
        } else if (id == R.id.nav_rewards) {
            return true;
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(Rewards.this, OrdersActivity.class));
            finish();
            return true;
        }
        return false;
    };
}