package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
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

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout layoutEmail, layoutPassword;

    private AppCompatImageButton loginButton;
    private TextView tvSignUp, tvSignInOnSignUp;

    private static final String BASE_URL = "https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/token?grant_type=password";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhlbmtqaXl3c2dqdGd0aXlmd3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcxMDkwMzEsImV4cCI6MjA2MjY4NTAzMX0.DkEOCkk34vyLZiJq7ivhU0XUIT8l7Z7pu7pP21TF2XU";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in); // замени на имя твоего XML файла

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        layoutEmail = findViewById(R.id.emailInputLayout);
        layoutPassword = findViewById(R.id.passwordInputLayout);

        loginButton = findViewById(R.id.loginButton);
        tvSignUp = findViewById(R.id.tvSignUp);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignIn.this, SignUp.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                startActivity(new Intent(SignIn.this, MainScreen.class));

            }
        });
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            layoutEmail.setError("Email is required");
            isValid = false;
        } else {
            layoutEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError("Password is required");
            isValid = false;
        } else {
            layoutPassword.setError(null);
        }

        if (!isValid) return;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("apikey", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(SignIn.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignIn.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(SignIn.this, "Login failed: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}