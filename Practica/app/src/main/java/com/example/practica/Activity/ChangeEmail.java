package com.example.practica.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.practica.Managers.AuthManager;
import com.example.practica.R;
import com.google.android.material.textfield.TextInputEditText;
import okhttp3.*;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

public class ChangeEmail extends AppCompatActivity {
    private AuthManager authManager;

    private TextInputEditText newEmailInput;
    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String SUPABASE_URL = "https://xenkjiywsgjtgtiyfwxg.supabase.co/rest/v1/profiles?id=eq.";
    private static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
    public static String REST_PATH = "rest/v1/";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_email);

        newEmailInput = findViewById(R.id.newEmail);
        findViewById(R.id.changeEmailButton).setOnClickListener(v -> {String newEmail = newEmailInput.getText().toString().trim();
        if (isValidEmail(newEmail)) {
            SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
            String accessToken = prefs.getString("access_token", null);
            if (accessToken != null) {
                performEmailUpdate(newEmail, accessToken);
            } else {
                Toast.makeText(this, "Please RE Auth", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SignIn.class);
                startActivity(intent);
                finish();

            }
        } else {
            showEmailError();
        }
    });
        authManager = new AuthManager(this);
    }

    private void performEmailUpdate(String newEmail, String accessToken) {
        String userId = authManager.getCurrentUserId();

        if (userId == null || userId.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Please RE Auth", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SignIn.class);
                startActivity(intent);
                finish();
            });
            return;
        }

        Log.d("EmailUpdate", "Updating email for user: " + userId);

        updateAuthEmail(newEmail, accessToken, new SBC_Callback() {
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    Log.e("Auth update failed", e.getMessage().toString());
                });

            }

            @Override
            public void onResponse(String responseBody) {
                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = getSharedPreferences("my_app_data", MODE_PRIVATE).edit();
                    editor.putString("user_email", newEmail);
                    editor.apply();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_email", newEmail);
                    setResult(RESULT_OK, resultIntent);
                    finish();

                });
            }
        });
    }
    public interface SBC_Callback {
        void onFailure(IOException e);
        void onResponse(String responseBody);
    }
    public void updateAuthEmail(String newEmail,String acessToken, SBC_Callback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("target_user_id", authManager.getCurrentUserId());
            jsonBody.put("new_email", newEmail);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH +"rpc/change_user_email_verified")
                .post(body)
                .addHeader("apikey", getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + acessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Server error: " + response.code()));
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }

        String[] parts = email.split("@");
        if (parts.length != 2) return false;

        String domainPart = parts[1];
        String[] domainParts = domainPart.split("\\.");
        if (domainParts.length < 2) return false;

        if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
            return false;
        }

        return true;
    }

    private void showEmailError() {
        newEmailInput.setError("Please enter a valid email (format: name@domain.com)");
        newEmailInput.requestFocus();
    }
}