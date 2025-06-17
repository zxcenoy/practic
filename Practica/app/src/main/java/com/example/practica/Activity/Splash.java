package com.example.practica.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.practica.Managers.AuthManager;
import com.example.practica.R;

public class Splash extends AppCompatActivity {

    private AuthManager authManager;
    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authManager.isLoggedIn()) {
                if (authManager.hasPinForCurrentUser()) {
                    startActivity(new Intent(Splash.this, PinCode.class));
                } else {
                    startActivity(new Intent(Splash.this, SetPin.class));
                }
            } else {
                startActivity(new Intent(Splash.this, OnboardingOne.class));
            }
            finish();

        }, SPLASH_DELAY);
    }
}