package com.example.practica.Activity;

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

import com.example.practica.Managers.AuthManager;
import com.example.practica.R;

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
            Toast.makeText(this, getString(R.string.PleaseEnterYourAddress), Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authManager.getCurrentUserId();

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

            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
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
                    Log.e("Network error: " , e.getMessage().toString());
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
    }
    public void BackProfile (View view){
        startActivity(new Intent(this, Profile.class));
    }
}