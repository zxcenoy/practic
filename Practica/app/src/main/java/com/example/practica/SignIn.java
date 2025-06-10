package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
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

    private static final String BASE_URL = "https://xenkjiywsgjtgtiyfwxg.supabase.co/auth/v1/token?grant_type=password";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhlbmtqaXl3c2dqdGd0aXlmd3hnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcxMDkwMzEsImV4cCI6MjA2MjY4NTAzMX0.DkEOCkk34vyLZiJq7ivhU0XUIT8l7Z7pu7pP21TF2XU";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        authManager = new AuthManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        layoutEmail = findViewById(R.id.emailInputLayout);
        layoutPassword = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);

        findViewById(R.id.tvSignUp).setOnClickListener(v ->
                startActivity(new Intent(SignIn.this, SignUp.class)));

        loginButton.setOnClickListener(v -> {
            if (validateFields()) {
                signIn();
            }
        });
    }

    private boolean validateFields() {
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

        return isValid;
    }

    private void signIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
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
                runOnUiThread(() ->
                        Toast.makeText(SignIn.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("SignIn", "Response Body: " + responseBody);

                        JSONObject obj = new JSONObject(responseBody);
                        String userId = obj.getJSONObject("user").getString("id");

                        runOnUiThread(() -> {
                            authManager.saveUserId(userId);
                            authManager.setLoggedIn(true);

                            if (authManager.hasPinForCurrentUser()) {
                                startActivity(new Intent(SignIn.this, PinCode.class));
                            } else {
                                startActivity(new Intent(SignIn.this, SetPin.class));
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(SignIn.this, "Ошибка разбора ответа", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(SignIn.this, "Login failed: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}