package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;


public class OnboardingThree extends AppCompatActivity {
    private ImageButton nextButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.onboarding_3);

        nextButton3 = findViewById(R.id.nextButton3);
        nextButton3.setOnClickListener(v -> {

            Intent intent = new Intent(OnboardingThree.this, SignIn.class);
            startActivity(intent);
        });
    }
}
