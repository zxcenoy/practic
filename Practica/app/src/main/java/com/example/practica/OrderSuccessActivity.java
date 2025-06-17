package com.example.practica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        Button btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnTrackOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
            finish();
        });
    }
}
