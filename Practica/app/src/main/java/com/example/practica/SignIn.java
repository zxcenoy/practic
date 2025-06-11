package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignIn extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout emailInputLayout, passwordInputLayout;

    private static final String BASE_URL = "https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/token?grant_type=password";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhlbmtqaXl3c2dqdGd0aXlmd3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcxMDkwMzEsImV4cCI6MjA2MjY4NTAzMX0.DkEOCkk34vyLZiJq7ivhU0XUIT8l7Z7pu7pP21TF2XU";


    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        AppCompatImageButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> signIn());
    }

    public void ForgotPasswordClick(View view) {
        startActivity(new Intent(this, ForgotPassword.class));
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(RequestBody.create(jsonBody.toString(), JSON))
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SignIn.this, "Network error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignIn.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignIn.this, PinCode.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(SignIn.this, "Login failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}