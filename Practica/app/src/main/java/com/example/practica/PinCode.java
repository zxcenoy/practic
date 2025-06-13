package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PinCode extends AppCompatActivity {

    private EditText[] pinFields = new EditText[4];
    private int currentFieldIndex = 0;
    private StringBuilder enteredPin = new StringBuilder();
    private AuthManager authManager;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_code);

        authManager = new AuthManager(this);

        pinFields[0] = findViewById(R.id.pinField1);
        pinFields[1] = findViewById(R.id.pinField2);
        pinFields[2] = findViewById(R.id.pinField3);
        pinFields[3] = findViewById(R.id.pinField4);

        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);

        titleText.setText("PIN CODE");
        subtitleText.setText("Enter your PIN code");

        setupNumberButtons();
        setupBackspaceButton();
    }

    private void setupNumberButtons() {
        for (int i = 0; i <= 9; i++) {
            int id = getResources().getIdentifier("btn" + i, "id", getPackageName());
            AppCompatButton button = findViewById(id);
            if (button != null) {
                final String digit = String.valueOf(i);
                button.setOnClickListener(v -> addDigit(digit));
            }
        }
    }

    private void setupBackspaceButton() {
        AppCompatImageButton btnBackspace = findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(v -> removeLastDigit());
    }

    private void addDigit(String digit) {
        if (currentFieldIndex >= pinFields.length)
            return;

        pinFields[currentFieldIndex].setText(digit);
        setFieldBackgroundTint(pinFields[currentFieldIndex], R.color.colorPrimary);

        enteredPin.append(digit);
        currentFieldIndex++;

        if (currentFieldIndex == pinFields.length) {
            String correctPin = authManager.getPinForCurrentUser();

            if (enteredPin.toString().equals(correctPin)) {
                for (EditText field : pinFields) {
                    setFieldBackgroundTint(field, R.color.green);
                }

                handler.postDelayed(() -> {
                    startActivity(new Intent(PinCode.this, MainScreen.class));
                    finish();
                }, 2500);

            } else {
                for (EditText field : pinFields) {
                    setFieldBackgroundTint(field, R.color.red);
                }

                handler.postDelayed(this::resetFields, 2500);
            }
        }
    }

    private void setFieldBackgroundTint(EditText field, int colorRes) {
        if (field != null) {
            field.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            field.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(colorRes)));
        }
    }

    private void removeLastDigit() {
        if (currentFieldIndex > 0) {
            currentFieldIndex--;
            enteredPin.deleteCharAt(currentFieldIndex);
            pinFields[currentFieldIndex].setText("");
            setFieldBackgroundTint(pinFields[currentFieldIndex], R.color.gray);
        }
    }

    private void resetFields() {
        enteredPin.setLength(0);
        currentFieldIndex = 0;

        for (EditText field : pinFields) {
            field.setText("");
            setFieldBackgroundTint(field, R.color.gray);
        }
    }
}