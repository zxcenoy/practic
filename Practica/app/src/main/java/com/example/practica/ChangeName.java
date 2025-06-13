package com.example.practica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;

public class ChangeName extends AppCompatActivity {
    private AuthManager authManager;

    private EditText newNameInput;
    private Button confirmButton;


    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DOMAIN_NAME = "https://xenkjiywsgjtgtiyfwxg.supabase.co/";
    public static String REST_PATH = "rest/v1/";
    public static String AUTH_PATH = "auth/v1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_name);

        newNameInput = findViewById(R.id.newName);
        confirmButton = findViewById(R.id.changeNameButton);


        confirmButton.setOnClickListener(v -> updateName());
        authManager = new AuthManager(this);

    }

    private void updateName() {
        String newName = newNameInput.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter your new name", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileUpdate profileUpdate = new ProfileUpdate(newName, null);

        sendProfileUpdate(profileUpdate,  newName);
    }

    private void sendProfileUpdate(ProfileUpdate profile, String newName) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);
        String userId = authManager.getCurrentUserId();

        if (userId == null || accessToken == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        String json;
        try {
            json = gson.toJson(profile);
            Log.d("ChangeName", "Request JSON: " + json); // Логируем запрос
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(json, JSON);
        HttpUrl url = HttpUrl.parse(DOMAIN_NAME + REST_PATH + "profiles")
                .newBuilder()
                .addQueryParameter("id", "eq." + userId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", getString(R.string.supabase_anon_key))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .build();

        // 5. Выполняем запрос
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("ChangeName", "Network error", e);
                    Toast.makeText(ChangeName.this,
                            "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_name", newName);
                        editor.apply();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("new_name", newName);
                        setResult(RESULT_OK, resultIntent);
                        finish();

                        Toast.makeText(ChangeName.this,
                                "Name updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ChangeName", "Error! Code: " + response.code() +
                                ", Body: " + responseBody);

                        String errorMsg = "Error updating name";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", errorMsg);
                        } catch (Exception e) {
                            Log.e("ChangeName", "Error parsing error response", e);
                        }

                        Toast.makeText(ChangeName.this,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
                if (response.body() != null) {
                    response.body().close();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}