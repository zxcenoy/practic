package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class OnboardingTwo extends AppCompatActivity {
    private ImageButton nextButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.onboarding_2);

        nextButton2 = findViewById(R.id.nextButton2);
        nextButton2.setOnClickListener(v -> {

            Intent intent = new Intent(OnboardingTwo.this, OnboardingThree.class);
            startActivity(intent);
        });
    }
}
