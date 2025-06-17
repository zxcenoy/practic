package com.example.practica.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.practica.R;

public class OnboardingOne extends AppCompatActivity {
    private ImageButton nextButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.onboarding_1);

        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {

            Intent intent = new Intent(OnboardingOne.this, OnboardingTwo.class);
            startActivity(intent);
        });
    }

}
