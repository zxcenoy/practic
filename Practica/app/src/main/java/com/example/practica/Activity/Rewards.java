package com.example.practica.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.practica.Adapters.LoyaltyAdapter;
import com.example.practica.Adapters.RewardHistoryAdapter;
import com.example.practica.Classes.LoyaltyCup;
import com.example.practica.R;
import com.example.practica.Items.RewardItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Rewards extends AppCompatActivity {
    private TextView myPointsText;
    private List<RewardItem> rewardHistory = new ArrayList<>();
    private static final String PREFS_NAME = "RewardsPrefs";
    private static final String KEY_POINTS = "total_points";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rewards);

        myPointsText = findViewById(R.id.myPointsText);
        RecyclerView rewardHistoryRecyclerView = findViewById(R.id.rewardHistoryRecyclerView);
        rewardHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        generateRewardHistory();




        RewardHistoryAdapter adapter = new RewardHistoryAdapter(rewardHistory);
        rewardHistoryRecyclerView.setAdapter(adapter);
        updatePointsDisplay();

        RecyclerView loyaltyRecyclerView = findViewById(R.id.loyaltyRecyclerView);
        loyaltyRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        List<LoyaltyCup> loyaltyCups = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            loyaltyCups.add(new LoyaltyCup(i < 4));
        }

        LoyaltyAdapter loyaltyAdapter = new LoyaltyAdapter(loyaltyCups);
        loyaltyRecyclerView.setAdapter(loyaltyAdapter);




        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_rewards);
    }


    private void generateRewardHistory() {
        Calendar calendar = Calendar.getInstance();

        rewardHistory.add(new RewardItem("Americano", 12, getDate(calendar, -1)));
        rewardHistory.add(new RewardItem("Cappuccino", 12, getDate(calendar, -2)));
        rewardHistory.add(new RewardItem("Mocha", 12, getDate(calendar, -3)));
        rewardHistory.add(new RewardItem("Flat White", 12, getDate(calendar, -4)));
    }
    private void updatePointsDisplay() {
        int totalPoints = calculateTotalPoints();
        myPointsText.setText(String.format(Locale.getDefault(), "My Points: %d", totalPoints));
    }
    private int calculateTotalPoints() {
        int total = 0;
        for (RewardItem item : rewardHistory) {
            if (item != null) {
                total += item.getPoints();
            }
        }
        return total;
    }

    private Date getDate(Calendar calendar, int daysOffset) {
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        return calendar.getTime();
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