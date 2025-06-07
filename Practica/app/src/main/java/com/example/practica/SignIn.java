package com.example.practica;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignIn extends AppCompatActivity {
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign_in);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {

            Intent intent = new Intent(SignIn.this, OnboardingThree.class);
            startActivity(intent);
        });
    }
}
