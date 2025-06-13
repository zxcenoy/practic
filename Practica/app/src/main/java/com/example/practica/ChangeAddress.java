package com.example.practica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChangeAddress extends AppCompatActivity {
    private static final String TAG = "ChangeAddress";
    private AuthManager authManager;
    private EditText newAddressInput;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
    private static final String REST_PATH = "rest/v1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_adress);

        newAddressInput = findViewById(R.id.newAddress);
        Button confirmButton = findViewById(R.id.changeAddressButton);
        authManager = new AuthManager(this);

        confirmButton.setOnClickListener(v -> updateAddress());
    }

    private void updateAddress() {
        String newAddress = newAddressInput.getText().toString().trim();
        if (newAddress.isEmpty()) {
            Toast.makeText(this, "Please enter your address", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("address", newAddress);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);
        if (accessToken == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(DOMAIN_NAME + REST_PATH + "profiles?id=eq." + userId)
                .patch(body)
                .addHeader("apikey", getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Network error", e);
                    Toast.makeText(ChangeAddress.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        saveAndReturnAddress(newAddress);
                    } else {
                        Log.e(TAG, "Server error: " + response.code() + ", " + responseBody);
                        Toast.makeText(ChangeAddress.this,
                                "Error updating address: " + responseBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void saveAndReturnAddress(String newAddress) {
        SharedPreferences.Editor editor = getSharedPreferences("my_app_data", MODE_PRIVATE).edit();
        editor.putString("user_address", newAddress);
        editor.apply();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_address", newAddress);
        setResult(RESULT_OK, resultIntent);
        finish();

        Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
    }
}