package com.example.practica.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.example.practica.Fragments.OrdersFragment;
import com.example.practica.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        OrdersPagerAdapter adapter = new OrdersPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Ongoing" : "History");
        }).attach();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.nav_orders);
    }


    private static class OrdersPagerAdapter extends FragmentStateAdapter {
        public OrdersPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return OrdersFragment.newInstance(position == 0 ? "1" : "4");
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(OrdersActivity.this, MainScreen.class));
            finish();
            return true;
        } else if (id == R.id.nav_rewards) {
            startActivity(new Intent(OrdersActivity.this, Rewards.class));
            finish();
            return true;
        } else if (id == R.id.nav_orders) {
            return true;
        }
        return false;
    };
}