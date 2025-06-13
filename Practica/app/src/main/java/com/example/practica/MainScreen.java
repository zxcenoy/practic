package com.example.practica;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainScreen extends AppCompatActivity {
    ImageButton btnProf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_screen);
        btnProf = findViewById(R.id.profileBtn);
        findViewById(R.id.profileBtn).setOnClickListener(v -> gotoProfile());

    }
    private void gotoProfile(){

            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);

    }

    public void BasketClick(View view){
        startActivity(new Intent(getApplicationContext(), MainScreen.class)); //TODO ПОМЕНЯЙ КОГДА СОЗДАШЬ КОРЗИНУ ЭКРАН
    }
}
