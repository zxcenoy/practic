package com.example.practica.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.practica.Classes.ProfileUpdate;
import com.example.practica.Managers.AuthManager;
import com.example.practica.R;
import com.google.gson.Gson;

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
            Toast.makeText(this, getString(R.string.PleaseEnterNewName), Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileUpdate profileUpdate = new ProfileUpdate(newName, null);

        sendProfileUpdate(profileUpdate,  newName);
    }

    private void sendProfileUpdate(ProfileUpdate profile, String newName) {

        SharedPreferences prefs = getSharedPreferences("my_app_data", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);
        String userId = authManager.getCurrentUserId();

        if (userId == null || accessToken == null) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        Gson gson = new Gson();
        String json;
            json = gson.toJson(profile);
            Log.d("ChangeName", "Request JSON: " + json);

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

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("ChangeName", "Network error", e);

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

                    } else {
                        Log.e("ChangeName", "Error! Code: " + response.code() +
                                ", Body: " + responseBody);

                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                        } catch (Exception e) {
                            Log.e("ChangeName", "Error parsing error response", e);
                        }

                    }
                });
                if (response.body() != null) {
                    response.body().close();
                }
            }
        });
    }
}